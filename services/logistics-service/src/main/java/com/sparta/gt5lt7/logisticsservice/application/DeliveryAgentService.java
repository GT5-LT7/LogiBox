package com.sparta.gt5lt7.logisticsservice.application;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryAgent;
import com.sparta.gt5lt7.logisticsservice.domain.entity.type.AgentType;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryAgentRepository;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRepository;
import com.sparta.gt5lt7.logisticsservice.global.exception.DeliveryAgentErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.DeliveryAgentException;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubException;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryAgentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryAgentService {

    private final DeliveryAgentRepository deliveryAgentRepository;
    private final HubRepository hubRepository;

    private static final int MAX_RETRY = 5;
    private static final String UK_USER = "uk_delivery_agents_user";

    @Transactional
    public DeliveryAgentResponse createDeliveryAgent(DeliveryAgentRequest request) {
        // 1. 한 사용자 = 한 배송 담당자 (선제 검증)
        if (deliveryAgentRepository.existsByUserIdAndDeletedAtIsNull(request.getUserId())) {
            throw new DeliveryAgentException(DeliveryAgentErrorCode.DELIVERY_AGENT_ALREADY_EXISTS);
        }

        // 2. 타입별 hub_id 결정
        UUID resolvedHubId;
        if (request.getAgentType() == AgentType.COMPANY_DELIVERY_AGENT) {
            if (request.getHubId() == null) {
                throw new DeliveryAgentException(
                        DeliveryAgentErrorCode.HUB_ID_REQUIRED_FOR_COMPANY_AGENT
                );
            }
            hubRepository.findByHubIdAndDeletedAtIsNull(request.getHubId())
                    .orElseThrow(() -> new HubException(HubErrorCode.HUB_NOT_FOUND));
            resolvedHubId = request.getHubId();
        } else {
            if (request.getHubId() != null) {
                throw new DeliveryAgentException(
                        DeliveryAgentErrorCode.HUB_ID_NOT_ALLOWED_FOR_HUB_AGENT
                );
            }
            resolvedHubId = null;
        }

        // 3. 시퀀스 충돌 대비 재시도 루프
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            int nextSequence = calculateNextSequence(request.getAgentType(), resolvedHubId);

            DeliveryAgent agent = DeliveryAgent.create(
                    request.getUserId(),
                    resolvedHubId,
                    request.getSlackUserId(),
                    request.getAgentType(),
                    nextSequence
            );

            try {
                return DeliveryAgentResponse.from(deliveryAgentRepository.saveAndFlush(agent));
            } catch (DataIntegrityViolationException e) {
                // user_id 중복은 재시도 무의미 → 즉시 409
                if (isUserUniqueViolation(e)) {
                    throw new DeliveryAgentException(
                            DeliveryAgentErrorCode.DELIVERY_AGENT_ALREADY_EXISTS
                    );
                }

                // 시퀀스 충돌이면 max+1 재계산 후 재시도
                log.warn("[DeliveryAgentService] 시퀀스 충돌, 재시도 {}/{}", attempt, MAX_RETRY);

                if (attempt == MAX_RETRY) {
                    log.error("[DeliveryAgentService] 시퀀스 할당 재시도 한도 초과", e);
                    throw e;
                }
            }
        }

        throw new IllegalStateException("배송 담당자 등록 재시도 한도 초과");
    }

    private int calculateNextSequence(AgentType type, UUID hubId) {
        Integer maxSeq;
        if (type == AgentType.HUB_DELIVERY_AGENT) {
            maxSeq = deliveryAgentRepository.findMaxSequenceForHubType(type);
        } else {
            maxSeq = deliveryAgentRepository.findMaxSequenceForCompanyType(type, hubId);
        }
        return maxSeq + 1;
    }

    // 예외가 user_id unique 제약 위반인지 판별. PostgreSQL은 메시지에 제약명을 포함하므로 contains 매칭.
    private boolean isUserUniqueViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause();
        if (cause == null) return false;
        String msg = cause.getMessage();
        return msg != null && msg.contains(UK_USER);
    }
}
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryAgentService {

    private final DeliveryAgentRepository deliveryAgentRepository;
    private final HubRepository hubRepository;

    @Transactional
    public DeliveryAgentResponse createDeliveryAgent(DeliveryAgentRequest request) {
        // 1. 한 사용자 = 한 배송 담당자
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
            // 허브 배송 담당자: 시스템 전체 소속 → hub_id가 들어오면 400으로 거절
            if (request.getHubId() != null) {
                throw new DeliveryAgentException(
                        DeliveryAgentErrorCode.HUB_ID_NOT_ALLOWED_FOR_HUB_AGENT
                );
            }
            resolvedHubId = null;
        }

        // 3. 다음 배송 순번 = 동일 scope 최대값 + 1
        int nextSequence = calculateNextSequence(request.getAgentType(), resolvedHubId);

        // 4. 저장 (동시성 경쟁으로 인한 유니크 제약 위반은 409로 변환)
        try {
            DeliveryAgent agent = DeliveryAgent.create(
                    request.getUserId(),
                    resolvedHubId,
                    request.getSlackUserId(),
                    request.getAgentType(),
                    nextSequence
            );
            return DeliveryAgentResponse.from(deliveryAgentRepository.save(agent));
        } catch (DataIntegrityViolationException e) {
            throw new DeliveryAgentException(DeliveryAgentErrorCode.DELIVERY_AGENT_ALREADY_EXISTS);
        }
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
}
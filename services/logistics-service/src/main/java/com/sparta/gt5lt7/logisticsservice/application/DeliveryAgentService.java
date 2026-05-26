package com.sparta.gt5lt7.logisticsservice.application;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryAgent;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryStatus;
import com.sparta.gt5lt7.logisticsservice.domain.entity.type.AgentType;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryAgentRepository;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryRepository;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRepository;
import com.sparta.gt5lt7.logisticsservice.global.exception.DeliveryAgentErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.DeliveryAgentException;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubException;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentUpdateRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryAgentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryAgentService {

    private final DeliveryAgentRepository deliveryAgentRepository;
    private final HubRepository hubRepository;
    private final DeliveryRepository deliveryRepository;

    private static final int MAX_RETRY = 5;
    private static final String UK_USER = "uk_delivery_agents_user";
    private static final String UK_SEQUENCE = "uk_delivery_agents_sequence_per_scope";

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

    @Transactional
    public DeliveryAgentResponse updateDeliveryAgent(
            UUID deliveryAgentId,
            DeliveryAgentUpdateRequest request
    ) {
        DeliveryAgent agent = deliveryAgentRepository
                .findByDeliveryAgentIdAndDeletedAtIsNull(deliveryAgentId)
                .orElseThrow(() -> new DeliveryAgentException(
                        DeliveryAgentErrorCode.DELIVERY_AGENT_NOT_FOUND
                ));

        boolean hubChanged = request.getHubId() != null
                && !Objects.equals(request.getHubId(), agent.getHubId());

        // 1. hubId 변경 검증 (실제로 다를 때만)
        if (hubChanged) {
            if (agent.getAgentType() == AgentType.HUB_DELIVERY_AGENT) {
                throw new DeliveryAgentException(
                        DeliveryAgentErrorCode.HUB_ID_NOT_ALLOWED_FOR_HUB_AGENT
                );
            }
            hubRepository.findByHubIdAndDeletedAtIsNull(request.getHubId())
                    .orElseThrow(() -> new HubException(HubErrorCode.HUB_NOT_FOUND));
        }

        // 2. slackUserId 변경 (시퀀스 영향 없음, 락 불필요)
        if (StringUtils.hasText(request.getSlackUserId())) {
            agent.updateSlackUserId(request.getSlackUserId());
        }

        // 3. hubId 변경 시 시퀀스 충돌 대비 재시도
        if (hubChanged) {
            for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
                int newSequence = calculateNextSequence(agent.getAgentType(), request.getHubId());
                agent.updateHubId(request.getHubId(), newSequence);
                try {
                    return DeliveryAgentResponse.from(deliveryAgentRepository.saveAndFlush(agent));
                } catch (DataIntegrityViolationException e) {
                    if (attempt == MAX_RETRY || !isSequenceUniqueViolation(e)) {
                        log.error("[DeliveryAgentService] 허브 이동 시 시퀀스 할당 실패", e);
                        throw e;
                    }
                    log.warn("[DeliveryAgentService] 시퀀스 충돌, 재시도 {}/{}", attempt, MAX_RETRY);
                }
            }
            throw new IllegalStateException("배송 담당자 시퀀스 재할당 한도 초과");
        }

        // hubId 변경 없는 경우 (slackUserId만 변경)
        return DeliveryAgentResponse.from(deliveryAgentRepository.saveAndFlush(agent));
    }

    @Transactional
    public DeliveryAgentResponse deleteDeliveryAgent(
            UUID deliveryAgentId,
            com.sparta.gt5lt7.common.security.CustomUserPrincipal principal
    ) {
        DeliveryAgent agent = deliveryAgentRepository
                .findByDeliveryAgentIdAndDeletedAtIsNull(deliveryAgentId)
                .orElseThrow(() -> new DeliveryAgentException(
                        DeliveryAgentErrorCode.DELIVERY_AGENT_NOT_FOUND
                ));

        // 진행 중 배송 확인 (DELIVERED, FAILED 외 상태)
        boolean hasOngoing = deliveryRepository
                .existsByDeliveryAgentIdAndDeliveryStatusNotInAndDeletedAtIsNull(
                        deliveryAgentId,
                        List.of(DeliveryStatus.DELIVERED, DeliveryStatus.FAILED)
                );
        if (hasOngoing) {
            throw new DeliveryAgentException(
                    DeliveryAgentErrorCode.AGENT_HAS_ONGOING_DELIVERY
            );
        }

        // 소프트 삭제
        agent.softDelete(principal.userId());

        return DeliveryAgentResponse.from(agent);
    }


    // 배송 담당자 단건 조회.
    public DeliveryAgentResponse getDeliveryAgent(UUID deliveryAgentId) {
        DeliveryAgent agent = deliveryAgentRepository
                .findByDeliveryAgentIdAndDeletedAtIsNull(deliveryAgentId)
                .orElseThrow(() -> new DeliveryAgentException(
                        DeliveryAgentErrorCode.DELIVERY_AGENT_NOT_FOUND
                ));
        return DeliveryAgentResponse.from(agent);
    }

    // 배송 담당자 검색·페이징, QueryDSL 기반, deleted_at IS NULL 자동 필터
    public Page<DeliveryAgentResponse> searchDeliveryAgents(
            DeliveryAgentSearchRequest request, Pageable pageable
    ) {
        return deliveryAgentRepository.searchDeliveryAgents(
                request.getHubId(),
                request.getAgentType(),
                request.getSlackUserId(),
                pageable
        ).map(DeliveryAgentResponse::from);
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

    private boolean isSequenceUniqueViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause();
        if (cause == null) return false;
        String msg = cause.getMessage();
        return msg != null && msg.contains(UK_SEQUENCE);
    }
}
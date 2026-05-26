package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryAgent;
import com.sparta.gt5lt7.logisticsservice.domain.entity.type.AgentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, UUID>, DeliveryAgentRepositoryCustom {

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<DeliveryAgent> findByDeliveryAgentIdAndDeletedAtIsNull(UUID deliveryAgentId);

    // HUB 타입 전체 최대 배송 순번 (deleted 행 포함)
    // 시퀀스가 단조증가하도록 삭제된 행도 함께 고려한다.
    @Query("SELECT COALESCE(MAX(d.deliverySequence), -1) FROM DeliveryAgent d " +
            "WHERE d.agentType = :agentType AND d.hubId IS NULL")
    Integer findMaxSequenceForHubType(@Param("agentType") AgentType agentType);

    // COMPANY 타입의 특정 허브 내 최대 배송 순번 (deleted 행 포함).
    @Query("SELECT COALESCE(MAX(d.deliverySequence), -1) FROM DeliveryAgent d " +
            "WHERE d.agentType = :agentType AND d.hubId = :hubId")
    Integer findMaxSequenceForCompanyType(
            @Param("agentType") AgentType agentType,
            @Param("hubId") UUID hubId
    );
}
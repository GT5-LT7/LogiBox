package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryAgent;
import com.sparta.gt5lt7.logisticsservice.domain.entity.type.AgentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, UUID> {

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);

    // HUB 타입 전체 최대 배송 순번 (hub_id IS NULL 활성 레코드 대상)
    @Query("SELECT COALESCE(MAX(d.deliverySequence), -1) FROM DeliveryAgent d " +
            "WHERE d.agentType = :agentType AND d.hubId IS NULL AND d.deletedAt IS NULL")
    Integer findMaxSequenceForHubType(@Param("agentType") AgentType agentType);

    // COMPANY 타입의 특정 허브 내 최대 배송 순번
    @Query("SELECT COALESCE(MAX(d.deliverySequence), -1) FROM DeliveryAgent d " +
            "WHERE d.agentType = :agentType AND d.hubId = :hubId AND d.deletedAt IS NULL")
    Integer findMaxSequenceForCompanyType(
            @Param("agentType") AgentType agentType,
            @Param("hubId") UUID hubId
    );
}
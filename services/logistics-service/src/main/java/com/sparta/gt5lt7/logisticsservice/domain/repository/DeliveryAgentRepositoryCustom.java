package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryAgent;
import com.sparta.gt5lt7.logisticsservice.domain.entity.type.AgentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DeliveryAgentRepositoryCustom {

    Page<DeliveryAgent> searchDeliveryAgents(
            UUID hubId,
            AgentType agentType,
            String slackUserId,
            Pageable pageable
    );
}
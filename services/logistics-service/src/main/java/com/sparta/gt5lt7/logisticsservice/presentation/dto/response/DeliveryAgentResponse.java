package com.sparta.gt5lt7.logisticsservice.presentation.dto.response;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryAgent;
import com.sparta.gt5lt7.logisticsservice.domain.entity.type.AgentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAgentResponse implements Serializable {

    private UUID deliveryAgentId;
    private UUID userId;
    private UUID hubId;
    private String slackUserId;
    private AgentType agentType;
    private Integer deliverySequence;
    private LocalDateTime createdAt;
    private UUID createdBy;

    public static DeliveryAgentResponse from(DeliveryAgent agent) {
        return DeliveryAgentResponse.builder()
                .deliveryAgentId(agent.getDeliveryAgentId())
                .userId(agent.getUserId())
                .hubId(agent.getHubId())
                .slackUserId(agent.getSlackUserId())
                .agentType(agent.getAgentType())
                .deliverySequence(agent.getDeliverySequence())
                .createdAt(agent.getCreatedAt())
                .createdBy(agent.getCreatedBy())
                .build();
    }
}
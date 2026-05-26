package com.sparta.gt5lt7.logisticsservice.presentation.dto.request;

import com.sparta.gt5lt7.logisticsservice.domain.entity.type.AgentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAgentSearchRequest {

    private UUID hubId;

    private AgentType agentType;

    private String slackUserId;
}
package com.sparta.gt5lt7.logisticsservice.presentation.dto.request;

import com.sparta.gt5lt7.logisticsservice.domain.entity.type.AgentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class DeliveryAgentRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private UUID userId;

    // COMPANY 타입에 한해 필수. 서비스 레이어에서 검증.
    private UUID hubId;

    @NotBlank(message = "Slack 사용자 ID는 필수입니다.")
    @Size(max = 100, message = "Slack 사용자 ID는 100자 이하여야 합니다.")
    private String slackUserId;

    @NotNull(message = "배송 담당자 타입은 필수입니다.")
    private AgentType agentType;
}
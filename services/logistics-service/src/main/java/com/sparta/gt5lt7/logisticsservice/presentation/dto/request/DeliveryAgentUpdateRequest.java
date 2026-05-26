package com.sparta.gt5lt7.logisticsservice.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class DeliveryAgentUpdateRequest {

    // COMPANY 타입의 소속 허브 변경. HUB 타입에는 사용 불가.
    private UUID hubId;

    @Size(max = 100, message = "Slack 사용자 ID는 100자 이하여야 합니다.")
    private String slackUserId;

    // 의도적으로 userId, agentType, deliverySequence 필드 없음
    //  - userId / agentType: 변경 불가 (DTO에 없으면 Jackson이 무시)
    //  - deliverySequence: 자동 관리 (요구사항: "재배열되지 않습니다")
}
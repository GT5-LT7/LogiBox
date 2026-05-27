package com.sparta.gt5lt7.order.application.dto;

import com.sparta.gt5lt7.order.domain.entity.RiskLevel;

import java.time.LocalDateTime;

public record AIDeliveryAnalysisResult(
        LocalDateTime finalDispatchDeadline,
        Integer estimatedTimeMinutes,
        Integer estimatedCost,
        RiskLevel riskLevel
) {
}
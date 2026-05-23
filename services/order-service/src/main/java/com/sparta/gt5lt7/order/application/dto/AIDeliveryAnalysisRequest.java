package com.sparta.gt5lt7.order.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AIDeliveryAnalysisRequest(
        UUID orderId,
        UUID deliveryId,
        UUID slackId,
        UUID productId,
        Integer quantity,
        String supplierHubName,
        String receiverHubName,
        LocalDateTime deliveryDeadline
) {
}

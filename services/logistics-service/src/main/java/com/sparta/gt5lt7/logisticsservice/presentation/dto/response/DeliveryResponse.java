package com.sparta.gt5lt7.logisticsservice.presentation.dto.response;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryResponse(
        UUID deliveryId,
        UUID orderId,
        UUID fromHubId,
        UUID toHubId,
        UUID receiverCompanyId,
        String receiverAddress,
        UUID deliveryAgentId,
        DeliveryStatus deliveryStatus,
        String requestMessage,
        LocalDateTime createdAt
) {
}
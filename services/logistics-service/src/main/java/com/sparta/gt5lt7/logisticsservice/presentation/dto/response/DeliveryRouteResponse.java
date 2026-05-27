package com.sparta.gt5lt7.logisticsservice.presentation.dto.response;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryRouteStatus;

import java.util.UUID;

public record DeliveryRouteResponse(
        UUID deliveryRouteId,
        Integer sequence,
        UUID fromHubId,
        UUID toHubId,
        Double estimatedDistance,
        Integer estimatedDuration,
        Double actualDistance,
        Integer actualDuration,
        DeliveryRouteStatus status
) {
}
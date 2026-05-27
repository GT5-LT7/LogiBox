package com.sparta.gt5lt7.logisticsservice.presentation.dto.request;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryStatus;

public record UpdateDeliveryStatusRequest(
        DeliveryStatus status
) {
}

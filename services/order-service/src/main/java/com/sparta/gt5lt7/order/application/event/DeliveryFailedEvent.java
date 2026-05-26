package com.sparta.gt5lt7.order.application.event;

import java.util.UUID;

public record DeliveryFailedEvent(
        UUID orderId,
        String reason
) {
}
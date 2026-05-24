package com.sparta.gt5lt7.order.application.event;

import java.util.UUID;

public record OrderCanceledEvent(
        UUID orderId,
        UUID deliveryId,
        UUID productId,
        Integer quantity
) {
}

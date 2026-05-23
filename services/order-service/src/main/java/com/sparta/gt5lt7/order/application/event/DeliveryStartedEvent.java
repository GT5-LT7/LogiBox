package com.sparta.gt5lt7.order.application.event;

import java.util.UUID;

public record DeliveryStartedEvent(
        UUID orderId
) {
}

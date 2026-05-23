package com.sparta.gt5lt7.order.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID supplierCompanyId,
        UUID receiverCompanyId,
        UUID productId,
        Integer quantity,
        LocalDateTime deliveryDeadline,
        String requestMessage
) {
}

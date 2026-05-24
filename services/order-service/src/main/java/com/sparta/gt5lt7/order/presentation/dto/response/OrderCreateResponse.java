package com.sparta.gt5lt7.order.presentation.dto.response;

import com.sparta.gt5lt7.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCreateResponse(
        UUID orderId,
        UUID supplierCompanyId,
        UUID receiverCompanyId,
        UUID productId,
        Integer quantity,
        OrderStatus orderStatus,
        LocalDateTime deliveryDeadline
) {
}
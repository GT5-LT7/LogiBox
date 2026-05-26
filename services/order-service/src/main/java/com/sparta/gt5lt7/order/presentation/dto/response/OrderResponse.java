package com.sparta.gt5lt7.order.presentation.dto.response;

import com.sparta.gt5lt7.order.domain.entity.Order;
import com.sparta.gt5lt7.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID supplierCompanyId,
        UUID receiverCompanyId,
        UUID productId,
        Integer quantity,
        UUID deliveryId,
        String requestMessage,
        LocalDateTime deliveryDeadline,
        OrderStatus orderStatus
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getSupplierCompanyId(),
                order.getReceiverCompanyId(),
                order.getProductId(),
                order.getQuantity(),
                order.getDeliveryId(),
                order.getRequestMessage(),
                order.getDeliveryDeadline(),
                order.getOrderStatus()
        );
    }
}

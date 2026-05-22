package com.sparta.gt5lt7.order.presentation.dto.response;

import com.sparta.gt5lt7.order.domain.entity.Order;
import com.sparta.gt5lt7.order.domain.entity.OrderStatus;
import java.util.UUID;

public class OrderResponse {

    public record Create(
            UUID orderId,
            String customerId,
            UUID storeId,
            UUID addressId,
            OrderStatus status,
            Integer totalPrice
    ) {
        public static Create from(Order order) {
            return new Create(
                    order.getId(),
                    order.getCustomerId(),
                    order.getStoreId(),
                    order.getAddressId(),
                    order.getStatus(),
                    order.getTotalPrice()
            );
        }
    }
}
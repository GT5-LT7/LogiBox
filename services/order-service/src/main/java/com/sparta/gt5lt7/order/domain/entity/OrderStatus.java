package com.sparta.gt5lt7.order.domain.entity;

public enum OrderStatus {
    PENDING,
    VALIDATING,
    WAITING_FOR_DELIVERY,
    SHIPPING,
    COMPLETED,
    CANCELED,
    REJECTED,
    FAILED
}

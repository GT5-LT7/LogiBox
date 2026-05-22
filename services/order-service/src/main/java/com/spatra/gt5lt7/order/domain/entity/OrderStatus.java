package com.spatra.gt5lt7.order.domain.entity;

import javax.annotation.processing.ProcessingEnvironment;

public enum OrderStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    PROCESSING,
    SHIPPING,
    COMPLETED,
    CANCELED
}

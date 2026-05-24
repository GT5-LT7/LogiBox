package com.sparta.gt5lt7.order.presentation.dto.request;

import com.sparta.gt5lt7.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderSearchCondition(
        OrderStatus orderStatus,
        UUID hubId,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
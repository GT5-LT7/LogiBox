package com.sparta.gt5lt7.logisticsservice.application.dto;

import com.sparta.gt5lt7.common.entity.UserRole;

import java.util.UUID;

public record DeliveryAccessContext(
        UUID userId,
        UserRole role,
        UUID hubId,
        UUID companyId
) {
}

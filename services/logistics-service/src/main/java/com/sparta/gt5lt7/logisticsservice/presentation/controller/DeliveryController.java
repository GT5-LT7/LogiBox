package com.sparta.gt5lt7.logisticsservice.presentation.controller;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.logisticsservice.application.dto.DeliveryAccessContext;
import com.sparta.gt5lt7.logisticsservice.application.service.DeliveryDeleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private final DeliveryDeleteService deliveryDeleteService;

    @DeleteMapping("/{deliveryId}")
    public ApiResponse<Void> deleteDelivery(
            @PathVariable UUID deliveryId,
            @AuthenticationPrincipal UUID userId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hub-Id", required = false) UUID hubId,
            @RequestHeader(value = "X-Company-Id", required = false) UUID companyId
    ) {
        DeliveryAccessContext context = new DeliveryAccessContext(
                userId,
                UserRole.fromString(role),
                hubId,
                companyId
        );

        deliveryDeleteService.deleteDelivery(deliveryId, context);

        return ApiResponse.deleted(null);
    }
}
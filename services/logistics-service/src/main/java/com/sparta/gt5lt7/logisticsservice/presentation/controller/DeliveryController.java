package com.sparta.gt5lt7.logisticsservice.presentation.controller;

import com.sparta.gt5lt7.common.dto.ApiResponse;
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
            @AuthenticationPrincipal UUID userId
    ) {
        deliveryDeleteService.deleteDelivery(deliveryId, userId);

        return ApiResponse.deleted(null);
    }
}
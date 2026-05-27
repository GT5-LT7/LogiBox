package com.sparta.gt5lt7.logisticsservice.presentation.controller;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.logisticsservice.application.dto.DeliveryAccessContext;
import com.sparta.gt5lt7.logisticsservice.application.service.DeliveryDeleteService;
import com.sparta.gt5lt7.logisticsservice.application.service.DeliveryQueryService;
import com.sparta.gt5lt7.logisticsservice.application.service.DeliveryUpdateService;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.UpdateDeliveryStatusRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryResponse;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private final DeliveryDeleteService deliveryDeleteService;
    private final DeliveryQueryService deliveryQueryService;
    private final DeliveryUpdateService deliveryUpdateService;

    // 배송 삭제
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

    // 배송 단건 조회
    @GetMapping("/{deliveryId}")
    public ApiResponse<DeliveryResponse> getDelivery(
            @PathVariable UUID deliveryId
    ) {
        return ApiResponse.success(
                deliveryQueryService.getDelivery(deliveryId)
        );
    }

    // 배송 경로 조회
    @GetMapping("/{deliveryId}/routes")
    public ApiResponse<List<DeliveryRouteResponse>> getDeliveryRoutes(
            @PathVariable UUID deliveryId
    ) {
        return ApiResponse.success(
                deliveryQueryService.getDeliveryRoutes(deliveryId)
        );
    }

    // 배송 상태 수정
    @PatchMapping("/{deliveryId}/status")
    public ApiResponse<Void> updateDeliveryStatus(
            @PathVariable UUID deliveryId,
            @RequestBody UpdateDeliveryStatusRequest request
    ) {
        deliveryUpdateService.updateStatus(
                deliveryId,
                request.status()
        );

        return ApiResponse.success(null);
    }
}
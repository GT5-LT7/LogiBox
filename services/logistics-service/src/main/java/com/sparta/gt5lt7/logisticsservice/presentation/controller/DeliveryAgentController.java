package com.sparta.gt5lt7.logisticsservice.presentation.controller;

import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.logisticsservice.application.DeliveryAgentService;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentUpdateRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryAgentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery-agents")
@RequiredArgsConstructor
public class DeliveryAgentController {

    private final DeliveryAgentService deliveryAgentService;
    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);

    private Pageable capSize(Pageable p) {
        return ALLOWED_SIZES.contains(p.getPageSize())
                ? p
                : PageRequest.of(p.getPageNumber(), 10, p.getSort());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<DeliveryAgentResponse>> createDeliveryAgent(
            @RequestBody @Valid DeliveryAgentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(deliveryAgentService.createDeliveryAgent(request)));
    }

    @PatchMapping("/{deliveryAgentId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<DeliveryAgentResponse>> updateDeliveryAgent(
            @PathVariable UUID deliveryAgentId,
            @RequestBody @Valid DeliveryAgentUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.updated(
                        deliveryAgentService.updateDeliveryAgent(deliveryAgentId, request)
                )
        );
    }

    @DeleteMapping("/{deliveryAgentId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<DeliveryAgentResponse>> deleteDeliveryAgent(
            @PathVariable UUID deliveryAgentId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                ApiResponse.deleted(
                        deliveryAgentService.deleteDeliveryAgent(deliveryAgentId, principal)
                )
        );
    }

    @GetMapping("/{deliveryAgentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeliveryAgentResponse>> getDeliveryAgent(
            @PathVariable UUID deliveryAgentId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(deliveryAgentService.getDeliveryAgent(deliveryAgentId))
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<DeliveryAgentResponse>>> searchDeliveryAgents(
            @ModelAttribute DeliveryAgentSearchRequest request,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(deliveryAgentService.searchDeliveryAgents(request, capSize(pageable)))
        );
    }
}
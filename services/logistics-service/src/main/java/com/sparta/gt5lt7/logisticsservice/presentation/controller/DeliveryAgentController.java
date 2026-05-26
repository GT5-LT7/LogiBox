package com.sparta.gt5lt7.logisticsservice.presentation.controller;

import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.logisticsservice.application.DeliveryAgentService;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryAgentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/delivery-agents")
@RequiredArgsConstructor
public class DeliveryAgentController {

    private final DeliveryAgentService deliveryAgentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<DeliveryAgentResponse>> createDeliveryAgent(
            @RequestBody @Valid DeliveryAgentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(deliveryAgentService.createDeliveryAgent(request)));
    }
}
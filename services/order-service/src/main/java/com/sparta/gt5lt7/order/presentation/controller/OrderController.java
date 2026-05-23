package com.sparta.gt5lt7.order.presentation.controller;

import com.sparta.gt5lt7.order.application.service.OrderService;
import com.sparta.gt5lt7.order.presentation.dto.request.OrderCreateRequest;
import com.sparta.gt5lt7.order.presentation.dto.response.OrderCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderCreateResponse createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal UUID userId
    ) {
        return orderService.createOrder(request, userId);
    }
}

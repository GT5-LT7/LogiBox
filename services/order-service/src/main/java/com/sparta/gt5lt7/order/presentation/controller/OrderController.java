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

import com.sparta.gt5lt7.order.domain.entity.OrderStatus;
import com.sparta.gt5lt7.order.presentation.dto.request.OrderSearchCondition;
import com.sparta.gt5lt7.order.presentation.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.sparta.gt5lt7.order.presentation.dto.request.OrderUpdateRequest;

import java.util.UUID;
import java.time.LocalDateTime;

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

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HUB_MANAGER', 'MASTER')")
    public Page<OrderResponse> getOrders(
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UUID userId
    ) {
        OrderSearchCondition condition = new OrderSearchCondition(
                orderStatus,
                hubId,
                startDate,
                endDate
        );

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return orderService.getOrders(condition, pageRequest, userId);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HUB_MANAGER', 'MASTER')")
    public OrderResponse getOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID userId
    ) {
        return orderService.getOrder(orderId, userId);
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse updateOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderUpdateRequest request,
            @AuthenticationPrincipal UUID userId
    ) {
        return orderService.updateOrder(
                orderId,
                request,
                userId
        );
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse cancelOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID userId
    ) {
        return orderService.cancelOrder(orderId, userId);
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MASTER')")
    public void deleteOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID userId
    ) {
        orderService.deleteOrder(orderId, userId);
    }
}

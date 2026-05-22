package com.sparta.gt5lt7.order.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(
        @NotNull UUID storeId,
        @NotNull UUID addressId,
        String request,
        @NotEmpty List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull UUID menuId,
            @NotNull @Min(1) Integer quantity
    ) {
    }
}
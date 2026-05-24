package com.sparta.gt5lt7.order.presentation.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record OrderUpdateRequest(

        @NotNull
        @Min(1)
        Integer quantity,

        String requestMessage,

        @NotNull
        @Future
        LocalDateTime deliveryDeadline
) {
}

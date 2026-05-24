package com.sparta.gt5lt7.order.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String code,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponse from(OrderException e) {
        return new ErrorResponse(
                e.getErrorCode().getStatus().value(),
                e.getErrorCode().name(),
                e.getMessage(),
                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getMessage(),
                LocalDateTime.now()
        );
    }
}
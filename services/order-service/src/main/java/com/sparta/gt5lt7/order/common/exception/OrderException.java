package com.sparta.gt5lt7.order.common.exception;

import lombok.Getter;

@Getter
public class OrderException extends RuntimeException {

    private final ErrorCode errorCode;

    public OrderException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public OrderException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
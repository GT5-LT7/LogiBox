package com.sparta.gt5lt7.common.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final ErrorCode errorCode;

    public BaseException(com.sparta.gt5lt7.logisticsservice.global.exception.@org.jetbrains.annotations.UnknownNullability DeliveryErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
package com.sparta.gt5lt7.logisticsservice.global.exception;

import lombok.Getter;

@Getter
public class DeliveryException extends RuntimeException {

    private final DeliveryErrorCode errorCode;

    public DeliveryException(DeliveryErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
package com.sparta.gt5lt7.logisticsservice.global.exception;

import com.sparta.gt5lt7.common.exception.BaseException;

public class DeliveryException extends BaseException {

    public DeliveryException(DeliveryErrorCode errorCode) {
        super(errorCode);
    }
}
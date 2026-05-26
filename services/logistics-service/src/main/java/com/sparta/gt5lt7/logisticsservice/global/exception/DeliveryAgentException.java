package com.sparta.gt5lt7.logisticsservice.global.exception;

import com.sparta.gt5lt7.common.exception.BaseException;

public class DeliveryAgentException extends BaseException {
    public DeliveryAgentException(DeliveryAgentErrorCode errorCode) {
        super(errorCode);
    }
}
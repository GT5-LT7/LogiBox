package com.sparta.gt5lt7.logisticsservice.global.exception;

import lombok.Getter;

@Getter
public class HubException extends RuntimeException {

    private final HubErrorCode errorCode;

    public HubException(HubErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
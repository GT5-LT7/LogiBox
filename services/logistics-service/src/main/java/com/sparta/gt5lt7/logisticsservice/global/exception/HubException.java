package com.sparta.gt5lt7.logisticsservice.global.exception;

import com.sparta.gt5lt7.common.exception.BaseException;

public class HubException extends BaseException {

    public HubException(HubErrorCode errorCode) {
        super(errorCode);
    }
}
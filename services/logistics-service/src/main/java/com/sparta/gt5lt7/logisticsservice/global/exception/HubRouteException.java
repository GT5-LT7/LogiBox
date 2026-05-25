package com.sparta.gt5lt7.logisticsservice.global.exception;

import com.sparta.gt5lt7.common.exception.BaseException;

public class HubRouteException extends BaseException {

    public HubRouteException(HubRouteErrorCode errorCode) {
        super(errorCode);
    }
}


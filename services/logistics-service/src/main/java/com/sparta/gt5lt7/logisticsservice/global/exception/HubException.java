package com.sparta.gt5lt7.logisticsservice.global.exception;

import lombok.Getter;

@Getter
public class HubException extends RuntimeException {

    private final HubErrorCode errorCode;

    /**
     * Create a HubException initialized with the provided HubErrorCode.
     *
     * The exception message is set from the error code's message and the supplied
     * error code is retained for later retrieval.
     *
     * @param errorCode the HubErrorCode that defines the exception message and code
     */
    public HubException(HubErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
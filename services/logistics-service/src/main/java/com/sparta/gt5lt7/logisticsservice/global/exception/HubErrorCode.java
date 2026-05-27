package com.sparta.gt5lt7.logisticsservice.global.exception;

import com.sparta.gt5lt7.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HubErrorCode implements ErrorCode {

    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "HUB-001", "존재하지 않거나 삭제된 허브입니다."),
    HUB_NAME_DUPLICATED(HttpStatus.CONFLICT, "HUB-002", "이미 존재하는 허브 이름입니다."),
    HUB_ACCESS_DENIED(HttpStatus.FORBIDDEN, "HUB-003", "해당 작업에 대한 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
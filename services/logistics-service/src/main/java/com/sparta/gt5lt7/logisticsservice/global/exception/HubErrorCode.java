package com.sparta.gt5lt7.logisticsservice.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HubErrorCode {

    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않거나 삭제된 허브입니다."),
    HUB_NAME_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 허브 이름입니다."),
    HUB_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 작업에 대한 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;
}
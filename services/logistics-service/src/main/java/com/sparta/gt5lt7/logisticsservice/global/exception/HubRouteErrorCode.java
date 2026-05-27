package com.sparta.gt5lt7.logisticsservice.global.exception;

import com.sparta.gt5lt7.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HubRouteErrorCode implements ErrorCode {

    HUB_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTE-001", "존재하지 않거나 삭제된 이동 경로입니다."),
    HUB_ROUTE_DUPLICATED(HttpStatus.CONFLICT, "ROUTE-002", "이미 등록된 이동 경로입니다."),
    HUB_ROUTE_SAME_HUB(HttpStatus.BAD_REQUEST, "ROUTE-003", "출발 허브와 도착 허브는 동일할 수 없습니다."),
    HUB_ROUTE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ROUTE-004", "해당 작업에 대한 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
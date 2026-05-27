package com.sparta.gt5lt7.logisticsservice.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryErrorCode {

    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "배송 정보를 찾을 수 없습니다."),
    DELIVERY_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "배송 경로 정보를 찾을 수 없습니다."),
    DELIVERY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "배송 접근 권한이 없습니다."),
    DELIVERY_DELETE_DENIED(HttpStatus.FORBIDDEN, "배송 삭제 권한이 없습니다."),
    DELIVERY_UPDATE_DENIED(HttpStatus.FORBIDDEN, "배송 수정 권한이 없습니다."),
    HUB_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "허브 배송 경로를 찾을 수 없습니다."),
    DUPLICATED_DELIVERY(HttpStatus.CONFLICT, "이미 생성된 배송 정보가 있습니다.");

    private final HttpStatus status;
    private final String message;
}
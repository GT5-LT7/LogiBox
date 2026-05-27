package com.sparta.gt5lt7.logisticsservice.global.exception;

import com.sparta.gt5lt7.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {

    DELIVERY_NOT_FOUND("DELIVERY_001", HttpStatus.NOT_FOUND, "배송 정보를 찾을 수 없습니다."),
    DELIVERY_ROUTE_NOT_FOUND("DELIVERY_002", HttpStatus.NOT_FOUND, "배송 경로 정보를 찾을 수 없습니다."),
    DELIVERY_ACCESS_DENIED("DELIVERY_003", HttpStatus.FORBIDDEN, "배송 접근 권한이 없습니다."),
    DELIVERY_DELETE_DENIED("DELIVERY_004", HttpStatus.FORBIDDEN, "배송 삭제 권한이 없습니다."),
    DELIVERY_UPDATE_DENIED("DELIVERY_005", HttpStatus.FORBIDDEN, "배송 수정 권한이 없습니다."),
    HUB_ROUTE_NOT_FOUND("DELIVERY_006", HttpStatus.NOT_FOUND, "허브 배송 경로를 찾을 수 없습니다."),
    DUPLICATED_DELIVERY("DELIVERY_007", HttpStatus.CONFLICT, "이미 생성된 배송 정보가 있습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
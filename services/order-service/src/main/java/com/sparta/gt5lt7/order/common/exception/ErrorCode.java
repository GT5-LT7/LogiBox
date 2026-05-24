package com.sparta.gt5lt7.order.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "허브를 찾을 수 없습니다."),
    REQUEST_OVERLOAD(HttpStatus.NOT_FOUND, "현재 주문이 많아 주문을 할 수 없습니다."),

    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),
    DELIVERY_CREATION_FAILED(HttpStatus.BAD_REQUEST, "배송 생성에 실패했습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "잘못된 주문 상태입니다."),
    DELETED_ORDER_ACCESS(HttpStatus.BAD_REQUEST, "삭제된 주문에는 접근할 수 없습니다."),

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    ORDER_STATUS_INVALID(HttpStatus.FORBIDDEN, "PENDING 상태에서만 수정 가능합니다."),

    RABBITMQ_MESSAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RabbitMQ 메시지 처리에 실패했습니다."),
    AI_API_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI API 응답 처리에 실패했습니다."),
    AI_RESPONSE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답 파싱에 실패했습니다."),
    EXTERNAL_SERVICE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "외부 서비스 호출에 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}

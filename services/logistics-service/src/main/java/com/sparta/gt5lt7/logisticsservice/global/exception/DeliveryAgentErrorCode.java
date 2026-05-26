package com.sparta.gt5lt7.logisticsservice.global.exception;

import com.sparta.gt5lt7.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryAgentErrorCode implements ErrorCode {

    DELIVERY_AGENT_NOT_FOUND(HttpStatus.NOT_FOUND, "AGENT-001", "존재하지 않거나 삭제된 배송 담당자입니다."),
    DELIVERY_AGENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "AGENT-002", "이미 등록된 사용자입니다."),
    HUB_ID_REQUIRED_FOR_COMPANY_AGENT(HttpStatus.BAD_REQUEST, "AGENT-003", "업체 배송 담당자는 소속 허브가 필수입니다."),
    DELIVERY_AGENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AGENT-004", "해당 작업에 대한 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
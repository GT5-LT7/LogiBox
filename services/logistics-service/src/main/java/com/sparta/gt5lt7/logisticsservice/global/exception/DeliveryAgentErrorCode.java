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
    DELIVERY_AGENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AGENT-004", "해당 작업에 대한 권한이 없습니다."),
    HUB_ID_NOT_ALLOWED_FOR_HUB_AGENT(HttpStatus.BAD_REQUEST, "AGENT-005", "허브 배송 담당자는 소속 허브를 지정할 수 없습니다."),
    AGENT_HAS_ONGOING_DELIVERY(HttpStatus.CONFLICT, "AGENT-007", "진행 중인 배송이 있어 삭제할 수 없습니다. 모든 배송 완료 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
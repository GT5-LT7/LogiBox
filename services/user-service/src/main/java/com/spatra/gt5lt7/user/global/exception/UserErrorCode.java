package com.spatra.gt5lt7.user.global.exception;

import com.spatra.gt5lt7.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "USER-002", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER-003", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER-004", "비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "USER-005", "권한이 없습니다."),
    INVALID_STATUS(HttpStatus.BAD_REQUEST, "USER-006", "유효하지 않은 계정 상태입니다."),
    NOT_APPROVED(HttpStatus.FORBIDDEN, "USER-007", "승인되지 않은 계정입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
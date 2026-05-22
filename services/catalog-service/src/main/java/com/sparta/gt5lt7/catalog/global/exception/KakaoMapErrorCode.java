package com.sparta.gt5lt7.catalog.global.exception;

import com.sparta.gt5lt7.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum KakaoMapErrorCode implements ErrorCode {
    // 401 UNAUTHORIZED
    API_KEY_INVALID(HttpStatus.UNAUTHORIZED, "MAP-002", "카카오 API 인증에 실패했습니다."),

    // 403 FORBIDDEN
    API_QUOTA_EXCEEDED(HttpStatus.FORBIDDEN, "MAP-003", "카카오 API 호출 횟수를 초과했습니다."),

    // 404 NOT_FOUND
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "MAP-001", "주소를 찾을 수 없습니다."),

    // 500 INTERNAL_SERVER_ERROR
    KAKAO_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MAP-004", "카카오 맵 서비스에 일시적인 오류가 발생했습니다."),

    // 503 SERVICE_UNAVAILABLE
    KAKAO_API_TIMEOUT(HttpStatus.SERVICE_UNAVAILABLE, "MAP-007", "카카오 API 호출 시간이 초과되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
package com.sparta.gt5lt7.catalog.global.advice;

import com.sparta.gt5lt7.catalog.global.exception.KakaoMapErrorCode;
import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.common.exception.ErrorCode;
import feign.FeignException;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1) // catalog 예외를 먼저 처리하도록 공통 모듈의 핸들러보다 높은 우선순위
@RestControllerAdvice
public class CatalogExceptionHandler {
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeignException(FeignException e) {
        int status = e.status();

        KakaoMapErrorCode errorCode = switch (status) {
            case 401 -> KakaoMapErrorCode.API_KEY_INVALID;
            case 403 -> KakaoMapErrorCode.API_QUOTA_EXCEEDED;
            case 503 -> KakaoMapErrorCode.KAKAO_API_TIMEOUT;
            default -> KakaoMapErrorCode.KAKAO_SERVER_ERROR;
        };

        return buildResponse(errorCode);
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(ErrorCode errorCode) {
        String message = String.format("[%s] %s", errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(message));
    }
}
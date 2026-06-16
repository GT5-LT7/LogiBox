package com.sparta.gt5lt7.common.advice;

import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.common.exception.CommonErrorCode;
import com.sparta.gt5lt7.common.exception.ErrorCode;
import com.sparta.gt5lt7.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        return buildResponse(e.getErrorCode());
    }

    // Validation 검증 실패 (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = CommonErrorCode.VALIDATION_FAILED;
        String message = String.format("[%s] %s", errorCode.getCode(), errorCode.getMessage());
        List<ApiResponse.ValidationError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiResponse.ValidationError(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message, errors));
    }

    // 접근 권한 없음 (403)
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AuthorizationDeniedException e) {
        return buildResponse(CommonErrorCode.FORBIDDEN);
    }

    // 경로를 찾을 수 없음 (404)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {
        return buildResponse(CommonErrorCode.NOT_FOUND);
    }

    // 지원하지 않는 HTTP 메서드 요청 (405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return buildResponse(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    // 낙관적 락 동시 수정 충돌 (409)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockException() {
        return buildResponse(CommonErrorCode.CONCURRENT_UPDATE_CONFLICT);
    }

    // 서버 내부 에러 (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return buildResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(ErrorCode errorCode) {
        String message = String.format("[%s] %s", errorCode.getCode(), errorCode.getMessage());
        HttpStatusCode status = errorCode.getStatus();
        return ResponseEntity.status(status).body(ApiResponse.error(status, message));
    }
}
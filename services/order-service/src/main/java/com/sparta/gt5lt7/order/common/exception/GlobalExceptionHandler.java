package com.sparta.gt5lt7.order.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> handleOrderException(OrderException e) {
        log.warn("OrderException: {}", e.getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.from(e));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("AccessDeniedException: {}", e.getMessage());

        return ResponseEntity
                .status(ErrorCode.ACCESS_DENIED.getStatus())
                .body(ErrorResponse.of(ErrorCode.ACCESS_DENIED));
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<ErrorResponse> handleRabbitMqException(AmqpException e) {
        log.error("RabbitMQ message error", e);

        return ResponseEntity
                .status(ErrorCode.RABBITMQ_MESSAGE_FAILED.getStatus())
                .body(ErrorResponse.of(ErrorCode.RABBITMQ_MESSAGE_FAILED));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        400,
                        "VALIDATION_FAILED",
                        e.getBindingResult().getAllErrors().get(0).getDefaultMessage(),
                        java.time.LocalDateTime.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error", e);

        return ResponseEntity
                .internalServerError()
                .body(new ErrorResponse(
                        500,
                        "INTERNAL_SERVER_ERROR",
                        "서버 내부 오류가 발생했습니다.",
                        java.time.LocalDateTime.now()
                ));
    }
}
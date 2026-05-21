package com.sparta.gt5lt7.catalog.global.exception;

import com.sparta.gt5lt7.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-001", "상품을 찾을 수 없습니다."),
    PRODUCT_CREATE_DENIED(HttpStatus.FORBIDDEN, "PRODUCT-002", "상품 등록 권한이 없습니다."),
    PRODUCT_UPDATE_DENIED(HttpStatus.FORBIDDEN, "PRODUCT-003", "상품 수정 권한이 없습니다."),
    PRODUCT_DELETE_DENIED(HttpStatus.FORBIDDEN, "PRODUCT-004", "상품 삭제 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
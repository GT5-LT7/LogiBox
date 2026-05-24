package com.sparta.gt5lt7.catalog.global.exception;

import com.sparta.gt5lt7.common.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CategoryErrorCode implements ErrorCode {
    // 404 NOT_FOUND
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY-001", "카테고리를 찾을 수 없습니다."),

    // 403 FORBIDDEN
    CATEGORY_CREATE_DENIED(HttpStatus.FORBIDDEN, "CATEGORY-002", "카테고리 등록 권한이 없습니다."),
    CATEGORY_UPDATE_DENIED(HttpStatus.FORBIDDEN, "CATEGORY-003", "카테고리 수정 권한이 없습니다."),
    CATEGORY_DELETE_DENIED(HttpStatus.FORBIDDEN, "CATEGORY-004", "카테고리 삭제 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
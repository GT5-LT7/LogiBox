package com.sparta.gt5lt7.catalog.global.exception;

import com.sparta.gt5lt7.common.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CompanyErrorCode implements ErrorCode {
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY-001", "업체를 찾을 수 없습니다."),
    COMPANY_CREATE_DENIED(HttpStatus.FORBIDDEN, "COMPANY-002", "업체 등록 권한이 없습니다."),
    COMPANY_UPDATE_DENIED(HttpStatus.FORBIDDEN, "COMPANY-003", "업체 수정 권한이 없습니다."),
    COMPANY_DELETE_DENIED(HttpStatus.FORBIDDEN, "COMPANY-004", "업체 삭제 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
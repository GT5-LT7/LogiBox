package com.sparta.gt5lt7.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ApiResponse<T> {
    private String message;
    private T data;
    private List<ValidationError> errors; // Validation 에러용

    // 성공 응답
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder().message("SUCCESS").build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().message("SUCCESS").data(data).build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder().message("CREATED").data(data).build();
    }

    public static <T> ApiResponse<T> updated(T data) {
        return ApiResponse.<T>builder().message("UPDATED").data(data).build();
    }

    public static <T> ApiResponse<T> deleted(T data) {
        return ApiResponse.<T>builder().message("DELETED").data(data).build();
    }

    public static <T> ApiResponse<T> accepted(T data) {
        return ApiResponse.<T>builder().message("ACCEPTED").data(data).build();
    }

    // 에러 응답
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().message(message).build();
    }

    public static <T> ApiResponse<T> error(String message, List<ValidationError> errors) {
        return ApiResponse.<T>builder().message(message).errors(errors).build();
    }

    @Getter
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
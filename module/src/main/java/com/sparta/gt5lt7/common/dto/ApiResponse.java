package com.sparta.gt5lt7.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

import java.util.List;

@Getter
@Builder
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private List<ValidationError> errors; // Validation 에러용

    // 성공 응답
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder().status(200).message("SUCCESS").build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().status(200).message("SUCCESS").data(data).build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder().status(201).message("CREATED").data(data).build();
    }

    public static <T> ApiResponse<T> updated(T data) {
        return ApiResponse.<T>builder().status(200).message("UPDATED").data(data).build();
    }

    public static <T> ApiResponse<T> deleted(T data) {
        return ApiResponse.<T>builder().status(200).message("DELETED").data(data).build();
    }

    public static <T> ApiResponse<T> accepted(T data) {
        return ApiResponse.<T>builder().status(202).message("ACCEPTED").data(data).build();
    }

    // 에러 응답
    public static <T> ApiResponse<T> error(HttpStatusCode status, String message) {
        return ApiResponse.<T>builder().status(status.value()).message(message).build();
    }

    public static <T> ApiResponse<T> error(String message, List<ValidationError> errors) {
        return ApiResponse.<T>builder().status(400).message(message).errors(errors).build();
    }

    @Getter
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
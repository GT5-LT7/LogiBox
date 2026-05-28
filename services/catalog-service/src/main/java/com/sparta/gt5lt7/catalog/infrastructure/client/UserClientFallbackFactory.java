package com.sparta.gt5lt7.catalog.infrastructure.client;

import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return ids -> {
            String errorMessage = (cause != null && cause.getMessage() != null) ? cause.getMessage() : "Unknown";
            log.error("[UserClient] 목록 조회 실패로 인한 폴백 실행. 대상 ID: {}, 원인: {}", ids, errorMessage);
            List<UserResponse> responses = ids.stream().map(id -> new UserResponse(id, "-")).toList();
            return ApiResponse.success(responses);
        };
    }
}
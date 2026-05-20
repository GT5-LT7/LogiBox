package com.sparta.gt5lt7.catalog.infrastructure.client;

import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class UserClientFallback implements UserClient {
    @Override
    public List<UserResponse> getUsers(Set<UUID> ids) {
        // TODO: /internal/users 구현이 완료되면 실제 에러 로깅 및 예외 처리 로직으로 대체
        return ids.stream()
                .map(id -> new UserResponse(id, "임시 사용자"))
                .toList();
    }
}
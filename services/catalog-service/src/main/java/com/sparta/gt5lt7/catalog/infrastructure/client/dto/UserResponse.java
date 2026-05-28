package com.sparta.gt5lt7.catalog.infrastructure.client.dto;

import java.util.Map;
import java.util.UUID;

public record UserResponse(UUID userId, String name) {
    public static UserResponse from(UUID userId, Map<UUID, UserResponse> userMap) {
        if (userMap.containsKey(userId)) {
            return userMap.get(userId);
        }
        return new UserResponse(userId, "탈퇴 회원");
    }

    public UUID id() {
        return this.userId;
    }
}
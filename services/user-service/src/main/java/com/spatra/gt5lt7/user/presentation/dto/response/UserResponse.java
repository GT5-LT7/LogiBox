package com.spatra.gt5lt7.user.presentation.dto.response;

import com.spatra.gt5lt7.user.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserResponse {

    private UUID userId;
    private String username;
    private String email;
    private String slackUserId;
    private String role;
    private String status;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .slackUserId(user.getSlackUserId())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }
}
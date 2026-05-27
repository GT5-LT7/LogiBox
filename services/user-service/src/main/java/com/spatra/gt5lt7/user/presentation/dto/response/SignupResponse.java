package com.spatra.gt5lt7.user.presentation.dto.response;

import com.spatra.gt5lt7.user.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class SignupResponse {

    private UUID userId;
    private String username;
    private String email;
    private String status;

    public static SignupResponse from(User user) {
        return SignupResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .build();
    }
}
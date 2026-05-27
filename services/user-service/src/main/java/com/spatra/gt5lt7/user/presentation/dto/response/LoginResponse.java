package com.spatra.gt5lt7.user.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private String username;
    private String role;

    public static LoginResponse of(String token, String username, String role) {
        return LoginResponse.builder()
                .token(token)
                .username(username)
                .role(role)
                .build();
    }
}
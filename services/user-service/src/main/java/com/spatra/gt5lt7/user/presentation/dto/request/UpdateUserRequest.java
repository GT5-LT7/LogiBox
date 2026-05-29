package com.spatra.gt5lt7.user.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateUserRequest {

    @NotBlank
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank
    private String slackUserId;

    @Size(min = 8, max = 15, message = "비밀번호는 8~15자여야 합니다.")
    private String password;   // 선택적 변경 (null이면 변경 안 함)
}
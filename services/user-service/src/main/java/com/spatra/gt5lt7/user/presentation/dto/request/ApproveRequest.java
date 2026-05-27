package com.spatra.gt5lt7.user.presentation.dto.request;

import com.spatra.gt5lt7.user.domain.entity.UserRole;
import com.spatra.gt5lt7.user.domain.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ApproveRequest {

    @NotNull
    private UserStatus status;   // APPROVED or REJECTED

    private UserRole role;       // 승인 시 역할 지정
}
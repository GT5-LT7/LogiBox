package com.sparta.gt5lt7.user.presentation.dto.request;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.user.domain.entity.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchCondition {
    private String username;    // 이름 검색
    private UserRole role;      // 역할 필터
    private UserStatus status;  // 상태 필터
}
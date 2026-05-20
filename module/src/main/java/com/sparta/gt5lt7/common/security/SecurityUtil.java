package com.sparta.gt5lt7.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.UUID;

public final class SecurityUtil {
    // 객체 생성 방지
    private SecurityUtil() {}

    // TODO [User 담당자 필독]: 실체 CustomUserDetails(업체 또는 허브 ID 포함)에 맞게 수정하셔야 합니다.
    public static UUID getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("인증 정보가 존재하지 않습니다.");
        }

        String userIdString = (String) authentication.getPrincipal();
        return UUID.fromString(userIdString);
    }

    // TODO [User 담당자 필독]: 실제 권한 검증 규칙에 맞게 수정하셔야 합니다.
    public static List<String> getCurrentUserRoles(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("인증 정보가 존재하지 않습니다.");
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}
package com.sparta.gt5lt7.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class InternalFeignHeaderInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        // Spring Security 컨텍스트에서 인증 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // TODO [Gateway 담당자 필독]: 실제 헤더 이름에 맞게 수정하셔야 합니다.
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal principal) {
            if (principal.userId() != null) {
                template.header("X-User-Id", principal.userId().toString());
            }
            if (principal.role() != null) {
                template.header("X-User-Role", principal.role().name());
            }

            if (principal.hubId() != null) {
                template.header("X-User-Management-Id", principal.hubId().toString());
            } else if (principal.companyId() != null) {
                template.header("X-User-Management-Id", principal.companyId().toString());
            }
        }
    }
}
package com.sparta.gt5lt7.common.security;

import com.sparta.gt5lt7.common.entity.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class GatewayAuthenticationFilter extends GenericFilterBean {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // TODO [Gateway 담당자 필독]: 실제 헤더 이름에 맞게 수정하셔야 합니다.
        // Gateway가 헤더에 넣어준 정보 추출
        String userIdStr = httpRequest.getHeader("X-User-Id");
        String roleStr = httpRequest.getHeader("X-User-Role");
        String managementIdStr = httpRequest.getHeader("X-User-Management-Id");

        if (userIdStr != null && roleStr != null) {
            CustomUserPrincipal principal = new CustomUserPrincipal(
                    UUID.fromString(userIdStr),
                    UserRole.fromString(roleStr),
                    (managementIdStr != null) ? UUID.fromString(managementIdStr) : null
            );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()
            );

            // Spring Security 컨텍스트에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }
}
package com.sparta.gt5lt7.catalog.infrastructure.client;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.common.security.InternalFeignHeaderInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("헤더 전파 인터셉터 테스트")
public class InternalFeignHeaderInterceptorTest {
    private RequestTemplate template;
    private InternalFeignHeaderInterceptor interceptor;

    @BeforeEach
    void setUp() {
        template = new RequestTemplate();
        interceptor = new InternalFeignHeaderInterceptor();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("성공: SecurityContext에 인증 정보가 있으면 요청 헤더에 주입")
    void test1() {
        // given
        UUID userId = UUID.randomUUID();
        UUID managementId = UUID.randomUUID();

        CustomUserPrincipal principal = CustomUserPrincipal.of(userId, UserRole.ROLE_HUB_MANAGER, managementId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        interceptor.apply(template);

        // then
        assertThat(template.headers().get("X-User-Id")).containsExactly(userId.toString());
        assertThat(template.headers().get("X-User-Role")).containsExactly(UserRole.ROLE_HUB_MANAGER.name());
        assertThat(template.headers().get("X-User-Management-Id")).containsExactly(managementId.toString());
    }

    @Test
    @DisplayName("성공: SecurityContext에 인증 정보가 없으면 헤더가 주입되지 않고 통과")
    void test2() {
        // when
        interceptor.apply(template);

        // then
        assertThat(template.headers().get("X-User-Id")).isNull();
        assertThat(template.headers().get("X-User-Role")).isNull();
        assertThat(template.headers().get("X-User-Management-Id")).isNull();
    }
}
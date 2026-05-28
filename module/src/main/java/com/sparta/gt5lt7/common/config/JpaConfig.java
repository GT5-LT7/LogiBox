package com.sparta.gt5lt7.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "userAuditorAware")
public class JpaConfig {

    @Bean
    public AuditorAware<UUID> userAuditorAware() {
        // TODO [User 담당자 필독]: 실제 로그인한 사용자 ID를 반환하도록 수정하셔야 합니다.
        return () -> Optional.of(UUID.randomUUID());
    }
}
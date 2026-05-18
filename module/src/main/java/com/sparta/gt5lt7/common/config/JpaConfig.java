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
        // TODO: 실제 로그인한 유저 ID를 반환하도록 담당자가 수정해야 함
        return () -> Optional.of(UUID.randomUUID());
    }
}
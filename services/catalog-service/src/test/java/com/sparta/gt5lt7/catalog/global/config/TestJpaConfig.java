package com.sparta.gt5lt7.catalog.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@TestConfiguration
@EnableJpaAuditing(auditorAwareRef = "loginUserAuditorAware")
public class TestJpaConfig {
    private static final UUID TEST_AUDITOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Bean
    public AuditorAware<UUID> loginUserAuditorAware() {
        return () -> Optional.of(TEST_AUDITOR_ID);
    }
}
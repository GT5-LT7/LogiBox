package com.sparta.gt5lt7.logisticsservice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class AuditConfig {

    /**
     * Supplies the current auditor's identifier for Spring Data auditing.
     *
     * <p>When an authenticated principal is present, returns an Optional containing the principal's
     * username; otherwise returns an Optional containing "system".</p>
     *
     * @return an Optional with the current auditor's username, or an Optional containing "system"
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }
            return Optional.of(authentication.getName());
        };
    }
}
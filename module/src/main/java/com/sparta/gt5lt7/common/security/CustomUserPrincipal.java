package com.sparta.gt5lt7.common.security;

import com.sparta.gt5lt7.common.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record CustomUserPrincipal(UUID userId, UserRole role, UUID managementId) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.toString()));
    }

    @Override public String getPassword() {
        return null;
    }

    @Override public String getUsername() {
        return String.valueOf(this.userId);
    }

    @Override public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    // 권한 검증 메서드
    public boolean isMaster() {
        return this.role == UserRole.ROLE_MASTER;
    }

    public boolean isAccessibleHub(UUID targetHubId) {
        if (isMaster()) {
            return true;
        }
        return this.managementId != null && this.managementId.equals(targetHubId);
    }

    public boolean isAccessibleCompany(UUID targetCompanyId) {
        if (isMaster()) {
            return true;
        }
        return this.managementId != null && this.managementId.equals(targetCompanyId);
    }
}
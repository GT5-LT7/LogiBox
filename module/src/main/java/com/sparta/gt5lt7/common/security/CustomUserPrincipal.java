package com.sparta.gt5lt7.common.security;

import com.sparta.gt5lt7.common.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record CustomUserPrincipal(UUID userId, UserRole role, UUID hubId, UUID companyId) implements UserDetails {
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
        return this.hubId != null && this.hubId.equals(targetHubId);
    }

    public boolean isAccessibleCompany(UUID targetCompanyId) {
        if (isMaster()) {
            return true;
        }
        return this.companyId != null && this.companyId.equals(targetCompanyId);
    }

    // 사용자 권한에 따라 허브 또는 업체 ID로 바인딩하는 메서드
    public static CustomUserPrincipal of(UUID userId, UserRole role, UUID managementId) {
        UUID hubId = (role == UserRole.ROLE_HUB_MANAGER) ? managementId : null;
        UUID companyId = (role == UserRole.ROLE_COMPANY_MANAGER) ? managementId : null;

        return new CustomUserPrincipal(userId, role, hubId, companyId);
    }
}
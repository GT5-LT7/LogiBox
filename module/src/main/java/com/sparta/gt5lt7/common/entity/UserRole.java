package com.sparta.gt5lt7.common.entity;

public enum UserRole {
    ROLE_MASTER, ROLE_HUB_MANAGER, ROLE_COMPANY_MANAGER, ROLE_DELIVERY_MANAGER;

    public static UserRole fromString(String role) {
        return UserRole.valueOf(role);
    }
}
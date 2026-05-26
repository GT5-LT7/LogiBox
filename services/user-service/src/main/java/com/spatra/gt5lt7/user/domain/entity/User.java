package com.spatra.gt5lt7.user.domain.entity;

import com.spatra.gt5lt7.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", nullable = false, unique = true, length = 10)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "slack_user_id", nullable = false, length = 100)
    private String slackUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    // 비밀번호 변경
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 회원 정보 수정
    public void updateInfo(String email, String slackUserId) {
        this.email = email;
        this.slackUserId = slackUserId;
    }

    // 역할 변경
    public void updateRole(UserRole role) {
        this.role = role;
    }

    // 계정 상태 변경 (승인/거절)
    public void updateStatus(UserStatus status) {
        this.status = status;
    }
}
package com.spatra.gt5lt7.user.application;

import com.spatra.gt5lt7.common.exception.BaseException;
import com.spatra.gt5lt7.common.security.JwtTokenProvider;
import com.spatra.gt5lt7.user.domain.entity.User;
import com.spatra.gt5lt7.user.domain.entity.UserRole;
import com.spatra.gt5lt7.user.domain.entity.UserStatus;
import com.spatra.gt5lt7.user.domain.repository.UserRepository;
import com.spatra.gt5lt7.user.global.exception.UserErrorCode;
import com.spatra.gt5lt7.user.presentation.dto.request.ApproveRequest;
import com.spatra.gt5lt7.user.presentation.dto.request.LoginRequest;
import com.spatra.gt5lt7.user.presentation.dto.request.SignupRequest;
import com.spatra.gt5lt7.user.presentation.dto.response.LoginResponse;
import com.spatra.gt5lt7.user.presentation.dto.response.SignupResponse;
import com.spatra.gt5lt7.user.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        // 중복 검사
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BaseException(UserErrorCode.DUPLICATE_USERNAME);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BaseException(UserErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 BCrypt 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 유저 생성 (PENDING 상태)
        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .email(request.getEmail())
                .slackUserId(request.getSlackUserId())
                .role(UserRole.COMPANY_MGR)   // 기본 역할
                .status(UserStatus.PENDING)    // 승인 대기
                .build();

        userRepository.save(user);
        log.info("[SIGNUP] username={}", user.getUsername());

        return SignupResponse.from(user);
    }

    // 로그인
    public LoginResponse login(LoginRequest request) {

        // 유저 조회
        User user = userRepository.findByUsernameAndDeletedAtIsNull(request.getUsername())
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BaseException(UserErrorCode.INVALID_PASSWORD);
        }

        // 승인 여부 확인
        if (user.getStatus() != UserStatus.APPROVED) {
            throw new BaseException(UserErrorCode.NOT_APPROVED);
        }

        // JWT 발급
        String token = jwtTokenProvider.createToken(
                user.getUserId(),
                user.getUsername(),
                user.getRole().name()
        );

        log.info("[LOGIN] username={}, role={}", user.getUsername(), user.getRole());

        return LoginResponse.of(token, user.getUsername(), user.getRole().name());
    }

    // 회원가입 승인/거절
    @Transactional
    public UserResponse approve(UUID userId, ApproveRequest request) {

        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        user.updateStatus(request.getStatus());

        // 승인 시 역할 지정
        if (request.getStatus() == UserStatus.APPROVED && request.getRole() != null) {
            user.updateRole(request.getRole());
        }

        log.info("[APPROVE] userId={}, status={}", userId, request.getStatus());

        return UserResponse.from(user);
    }

    // 단건 조회
    public UserResponse getUser(UUID userId) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    // 소프트 삭제
    @Transactional
    public void deleteUser(UUID userId, UUID deletedBy) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
        user.softDelete(deletedBy);
        log.info("[DELETE] userId={}", userId);
    }
}
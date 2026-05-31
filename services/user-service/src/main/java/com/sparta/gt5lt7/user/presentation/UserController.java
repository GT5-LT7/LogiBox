package com.sparta.gt5lt7.user.presentation;

import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.common.dto.PageResponse;
import com.sparta.gt5lt7.user.domain.entity.User;
import com.sparta.gt5lt7.user.application.UserService;
import com.sparta.gt5lt7.user.presentation.dto.request.UserSearchCondition;
import com.sparta.gt5lt7.user.infrastructure.security.UserDetailsImpl;
import com.sparta.gt5lt7.user.presentation.dto.request.ApproveRequest;
import com.sparta.gt5lt7.user.presentation.dto.request.LoginRequest;
import com.sparta.gt5lt7.user.presentation.dto.request.SignupRequest;
import com.sparta.gt5lt7.user.presentation.dto.request.UpdateUserRequest;
import com.sparta.gt5lt7.user.presentation.dto.response.LoginResponse;
import com.sparta.gt5lt7.user.presentation.dto.response.SignupResponse;
import com.sparta.gt5lt7.user.presentation.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입 (ALL)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(userService.signup(request)));
    }

    // 회원가입 승인/거절 (MASTER, HUB_MGR)
    @PostMapping("/signup/approve/{id}")
    @PreAuthorize("hasAuthority('ROLE_MASTER') or hasAuthority('ROLE_HUB_MANAGER')")  // ← 추가
    public ResponseEntity<ApiResponse<UserResponse>> approve(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.approve(id, request)));
    }

    // 로그인 (ALL)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.login(request)));
    }

    // 단건 조회 (MASTER, 본인)
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MASTER') or #id.toString() == #userId")  // ← 추가
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUser(id)));
    }

    // 소프트 삭제 (MASTER)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MASTER')")  // ← 추가
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.deleteUser(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.deleted(null));
    }
    // 사용자 수정 (MASTER 또는 본인)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MASTER') or #id.toString() == #userId")  // ← 추가
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    ) {
        return ResponseEntity.ok(
                ApiResponse.updated(
                        userService.updateUser(id, request, UUID.fromString(userId), userRole)
                )
        );
    }
    // 사용자 목록 + 검색 (MASTER)
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MASTER')")  // ← 수정
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @ModelAttribute UserSearchCondition condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<User> result = userService.searchUsers(condition, pageable);
        return ResponseEntity.ok(
                ApiResponse.success(
                        PageResponse.of(result, UserResponse::from)  // ← 여기서 변환
                )
        );
    }
}
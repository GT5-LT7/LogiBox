package com.sparta.gt5lt7.logisticsservice.presentation.controller;

import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.common.dto.PageResponse;
import com.sparta.gt5lt7.logisticsservice.application.HubService;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubUpdateRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;
    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);

    private Pageable capSize(Pageable p) {
        return ALLOWED_SIZES.contains(p.getPageSize())
                ? p
                : PageRequest.of(p.getPageNumber(), 10, p.getSort());
    }

    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<HubResponse>> createHub(
            @RequestBody @Valid HubRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(hubService.createHub(request)));
    }

    @GetMapping("/{hubId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HubResponse>> getHub(@PathVariable UUID hubId) {
        return ResponseEntity.ok(ApiResponse.success(hubService.getHub(hubId)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<HubResponse>>> searchHubs(
            @ModelAttribute HubSearchRequest request,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(hubService.searchHubs(request, capSize(pageable)))
        );
    }

    @PatchMapping("/{hubId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<HubResponse>> updateHub(
            @PathVariable UUID hubId,
            @RequestBody @Valid HubUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.updated(hubService.updateHub(hubId, request)));
    }

    @DeleteMapping("/{hubId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<HubResponse>> deleteHub(
            @PathVariable UUID hubId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.deleted(hubService.deleteHub(hubId, principal)));
    }
}
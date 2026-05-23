package com.sparta.gt5lt7.logisticsservice.presentation;

import com.sparta.gt5lt7.logisticsservice.application.HubService;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;

    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<HubResponse> createHub(
            @RequestBody @Valid HubRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hubService.createHub(request));
    }

    // 허브 상세 조회
    @GetMapping("/{hubId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HubResponse> getHub(@PathVariable UUID hubId) {
        return ResponseEntity.ok(hubService.getHub(hubId));
    }

    // 허브 목록 검색
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<HubResponse>> searchHubs(
            HubSearchRequest request,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(hubService.searchHubs(request, pageable));
    }
}
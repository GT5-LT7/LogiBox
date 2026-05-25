package com.sparta.gt5lt7.logisticsservice.presentation;

import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.common.dto.PageResponse;
import com.sparta.gt5lt7.logisticsservice.application.HubService;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubUpdateRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;

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
    public ResponseEntity<ApiResponse<PageResponse<HubResponse>>> searchHubs(
            HubSearchRequest request,
            @PageableDefault(size = 10, sort = "createdAt",  direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<HubResponse> response = PageResponse.of(
                hubService.searchHubs(request, pageable),
                Function.identity()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{hubId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<HubResponse>> updateHub(
            @PathVariable UUID hubId,
            @RequestBody @Valid HubUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.updated(hubService.updateHub(hubId, request)));
    }
}
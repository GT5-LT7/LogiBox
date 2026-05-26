package com.sparta.gt5lt7.logisticsservice.presentation.controller;

import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.logisticsservice.application.HubRouteService;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRouteRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRouteSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubRouteResponse;
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
@RequestMapping("/api/v1/hub-routes")
@RequiredArgsConstructor
public class HubRouteController {

    private final HubRouteService hubRouteService;

    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<HubRouteResponse>> createHubRoute(
            @RequestBody @Valid HubRouteRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(hubRouteService.createHubRoute(request)));
    }

    @GetMapping("/{routeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HubRouteResponse>> getHubRoute(@PathVariable UUID routeId) {
        return ResponseEntity.ok(ApiResponse.success(hubRouteService.getHubRoute(routeId)));
    }

    @GetMapping("/from/{fromHubId}/to/{toHubId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HubRouteResponse>> getHubRouteByHubs(
            @PathVariable UUID fromHubId,
            @PathVariable UUID toHubId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(hubRouteService.getHubRouteByHubs(fromHubId, toHubId))
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<HubRouteResponse>>> searchHubRoutes(
            @ModelAttribute HubRouteSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(hubRouteService.searchHubRoutes(request, pageable))
        );
    }
}
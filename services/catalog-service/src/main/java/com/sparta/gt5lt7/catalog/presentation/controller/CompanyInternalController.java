package com.sparta.gt5lt7.catalog.presentation.controller;

import com.sparta.gt5lt7.catalog.application.service.CompanyService;
import com.sparta.gt5lt7.catalog.presentation.dto.response.HubUsageStatusResponse;
import com.sparta.gt5lt7.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/companies")
@RequiredArgsConstructor
public class CompanyInternalController {
    private final CompanyService companyService;

    @GetMapping("/hubs/{hubId}")
    public ResponseEntity<ApiResponse<HubUsageStatusResponse>> checkHubUsage(
            @PathVariable UUID hubId
    ) {
        HubUsageStatusResponse response = companyService.checkHubUsage(hubId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
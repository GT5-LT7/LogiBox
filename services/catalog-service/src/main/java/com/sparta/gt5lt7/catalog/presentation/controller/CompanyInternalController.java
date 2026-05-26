package com.sparta.gt5lt7.catalog.presentation.controller;

import com.sparta.gt5lt7.catalog.application.facade.CompanyFacade;
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
    private final CompanyFacade companyFacade;

    @GetMapping("/hubs/{hubId}")
    public ResponseEntity<ApiResponse<HubUsageStatusResponse>> checkHubUsage(
            @PathVariable UUID hubId
    ) {
        HubUsageStatusResponse response = companyFacade.checkHubUsage(hubId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
package com.sparta.gt5lt7.catalog.presentation.controller;

import com.sparta.gt5lt7.catalog.application.facade.CompanyFacade;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.application.service.CompanyService;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.presentation.dto.request.CompanyRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyResponse;
import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyFacade companyFacade;
    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<CompanyResponse.Create>> createCompany(
            @Valid @RequestBody CompanyRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CompanyResponse.Create response = companyFacade.createCompany(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponse.Summary>>> searchCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CompanyType type,
            @RequestParam(required = false) UUID hubId,
            Pageable pageable
    ) {
        PageResponse<CompanyResponse.Summary> response = companyService.searchCompanies(keyword, type, hubId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse.Detail>> getCompany(
            @PathVariable UUID id
    ) {
        CompanyResponse.Detail response = companyFacade.getCompany(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<CompanyResponse.Update>> updateCompany(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CompanyResponse.Update response = companyService.updateCompany(id, request, principal);
        return ResponseEntity.ok(ApiResponse.updated(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<CompanyResponse.Delete>> deleteCompany(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CompanyResponse.Delete response = companyService.deleteCompany(id, principal);
        return ResponseEntity.ok(ApiResponse.deleted(response));
    }
}
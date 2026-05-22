package com.sparta.gt5lt7.catalog.presentation;

import com.sparta.gt5lt7.catalog.application.CompanyService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.sparta.gt5lt7.common.security.SecurityUtil;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<CompanyResponse.Create>> createCompany(
            @Valid @RequestBody CompanyRequest request,
            Authentication authentication
    ) {
        UUID hubId = SecurityUtil.getCurrentUser(authentication);
        List<String> roles = SecurityUtil.getCurrentUserRoles(authentication);

        CompanyResponse.Create response = companyService.createCompany(request, hubId, roles);
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
        CompanyResponse.Detail response = companyService.getCompany(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<CompanyResponse.Update>> updateCompany(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyRequest request,
            Authentication authentication
    ) {
        UUID hubOrCompanyId = SecurityUtil.getCurrentUser(authentication);
        List<String> roles = SecurityUtil.getCurrentUserRoles(authentication);

        CompanyResponse.Update response = companyService.updateCompany(id, request, hubOrCompanyId, roles);
        return ResponseEntity.ok(ApiResponse.updated(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<CompanyResponse.Delete>> deleteCompany(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userAndHubId = SecurityUtil.getCurrentUser(authentication);
        List<String> roles = SecurityUtil.getCurrentUserRoles(authentication);

        CompanyResponse.Delete response = companyService.deleteCompany(id, userAndHubId, roles);
        return ResponseEntity.ok(ApiResponse.deleted(response));
    }
}
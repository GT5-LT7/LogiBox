package com.sparta.gt5lt7.catalog.presentation;

import com.sparta.gt5lt7.catalog.application.CompanyService;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.presentation.dto.request.CompanyCreateRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyCreateResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyResponse;
import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyCreateResponse>> createCompany(
            @Valid @RequestBody CompanyCreateRequest request
    ) {
        CompanyCreateResponse response = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponse>>> searchCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CompanyType type,
            @RequestParam(required = false) UUID hubId,
            Pageable pageable
    ) {
        PageResponse<CompanyResponse> response = companyService.searchCompanies(keyword, type, hubId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
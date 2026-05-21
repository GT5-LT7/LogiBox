package com.sparta.gt5lt7.catalog.presentation;

import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.ProductResponse;
import com.sparta.gt5lt7.common.response.ApiResponse;
import com.sparta.gt5lt7.common.security.SecurityUtil;
import com.sparta.gt5lt7.catalog.application.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse.Create>> createProduct(
            @Valid @RequestBody ProductRequest.Create request,
            Authentication authentication
    ) {
        UUID hubOrCompanyId = SecurityUtil.getCurrentUser(authentication);
        List<String> roles = SecurityUtil.getCurrentUserRoles(authentication);

        ProductResponse.Create response = productService.createProduct(request, hubOrCompanyId, roles);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse.Update>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest.Update request,
            Authentication authentication
    ) {
        UUID hubOrCompanyId = SecurityUtil.getCurrentUser(authentication);
        List<String> roles = SecurityUtil.getCurrentUserRoles(authentication);

        ProductResponse.Update response = productService.updateProduct(id, request, hubOrCompanyId, roles);
        return ResponseEntity.ok(ApiResponse.updated(response));
    }
}
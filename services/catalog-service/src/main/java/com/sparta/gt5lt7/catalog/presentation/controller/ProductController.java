package com.sparta.gt5lt7.catalog.presentation.controller;

import com.sparta.gt5lt7.catalog.application.facade.ProductFacade;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.ProductResponse;
import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.common.dto.PageResponse;
import com.sparta.gt5lt7.catalog.application.service.ProductService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductFacade productFacade;
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse.Create>> createProduct(
            @Valid @RequestBody ProductRequest.Create request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ProductResponse.Create response = productFacade.createProduct(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse.Summary>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") Boolean salesOnly,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID hubId,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        PageResponse<ProductResponse.Summary> response = productFacade.searchProducts(
                keyword, salesOnly, companyId, hubId, pageable, principal
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse.Detail>> getProduct(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ProductResponse.Detail response = productFacade.getProduct(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse.Update>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest.Update request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ProductResponse.Update response = productFacade.updateProduct(id, request, principal);
        return ResponseEntity.ok(ApiResponse.updated(response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse.StatusUpdate>> updateProductStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest.StatusUpdate request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ProductResponse.StatusUpdate response = productFacade.updateProductStatus(id, request, principal);
        return ResponseEntity.ok(ApiResponse.updated(response));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse.StockUpdate>> updateProductQuantity(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest.StockUpdate request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ProductResponse.StockUpdate response = productService.updateProductQuantity(id, request, principal);
        return ResponseEntity.ok(ApiResponse.updated(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse.Delete>> deleteProduct(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ProductResponse.Delete response = productFacade.deleteProduct(id, principal);
        return ResponseEntity.ok(ApiResponse.deleted(response));
    }
}
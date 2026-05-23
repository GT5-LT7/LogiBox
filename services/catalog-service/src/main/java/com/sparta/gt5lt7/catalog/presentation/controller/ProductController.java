package com.sparta.gt5lt7.catalog.presentation.controller;

import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.ProductResponse;
import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.common.dto.PageResponse;
import com.sparta.gt5lt7.catalog.application.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ProductResponse.Create response = productService.createProduct(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse.Summary>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) UUID categoryId,
            Pageable pageable
    ) {
        PageResponse<ProductResponse.Summary> response = productService.searchProducts(
                keyword, companyId, hubId, categoryId, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse.Detail>> getProduct(
            @PathVariable UUID id
    ) {
        ProductResponse.Detail response = productService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse.Update>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest.Update request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ProductResponse.Update response = productService.updateProduct(id, request, principal);
        return ResponseEntity.ok(ApiResponse.updated(response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse.StatusUpdate>> updateProductStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest.StatusUpdate request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ProductResponse.StatusUpdate response = productService.updateProductStatus(id, request, principal);
        return ResponseEntity.ok(ApiResponse.updated(response));
    }

    @PatchMapping("/stock")
    public ResponseEntity<ApiResponse<List<ProductResponse.StockUpdate>>> updateProductQuantity(
            @Valid @RequestBody ProductRequest.StockUpdate requests
    ) {
        List<ProductResponse.StockUpdate> response = productService.updateProductQuantity(requests);
        return ResponseEntity.ok(ApiResponse.updated(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse.Delete>> deleteProduct(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ProductResponse.Delete response = productService.deleteProduct(id, principal);
        return ResponseEntity.ok(ApiResponse.deleted(response));
    }
}
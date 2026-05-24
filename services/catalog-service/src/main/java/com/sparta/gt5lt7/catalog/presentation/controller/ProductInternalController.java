package com.sparta.gt5lt7.catalog.presentation.controller;

import com.sparta.gt5lt7.catalog.application.ProductService;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.ProductResponse;
import com.sparta.gt5lt7.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class ProductInternalController {
    private final ProductService productService;

    @PatchMapping("/stock")
    public ResponseEntity<ApiResponse<List<ProductResponse.StockUpdate>>> updateProductQuantity(
            @Valid @RequestBody ProductRequest.OrderStockUpdate requests
    ) {
        List<ProductResponse.StockUpdate> response = productService.updateProductQuantityForOrder(requests);
        return ResponseEntity.ok(ApiResponse.updated(response));
    }
}
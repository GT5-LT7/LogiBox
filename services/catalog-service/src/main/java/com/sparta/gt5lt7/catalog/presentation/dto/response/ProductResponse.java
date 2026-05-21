package com.sparta.gt5lt7.catalog.presentation.dto.response;

import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.domain.entity.ProductStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductResponse {
    public record Info(
            UUID productId, String name, String description, ProductStatus status,
            CompanyResponse.Simple company, CategoryResponse.Simple category, Long price, Integer quantity
    ) {
        public static ProductResponse.Info from(Product product) {
            return new ProductResponse.Info(
                    product.getProductId(), product.getName(), product.getDescription(), product.getStatus(),
                    CompanyResponse.Simple.from(product.getCompany()), CategoryResponse.Simple.from(product.getCategory()),
                    product.getPrice(), product.getQuantity()
            );
        }
    }

    public record Create(Info info, LocalDateTime createdAt) {
        public static Create from(Product product) {
            return new Create(Info.from(product), product.getCreatedAt());
        }
    }

    public record Update(Info info, LocalDateTime updatedAt) {
        public static Update from(Product product) {
            return new Update(Info.from(product), product.getUpdatedAt());
        }
    }
}
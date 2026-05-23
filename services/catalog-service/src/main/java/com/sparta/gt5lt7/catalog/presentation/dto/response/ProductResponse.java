package com.sparta.gt5lt7.catalog.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.domain.entity.ProductStatus;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductResponse {
    public record Create(UUID productId, String name, LocalDateTime createdAt) {
        public static Create from(Product product) {
            return new Create(product.getProductId(), product.getName(), product.getCreatedAt());
        }
    }

    public record Info(
            UUID productId, String name, String description, ProductStatus status,
            CompanyResponse.Simple company, HubResponse hub, CategoryResponse.Simple category,
            Long price, Integer quantity
    ) {
        public static ProductResponse.Info of(Product product, HubResponse hub) {
            return new ProductResponse.Info(
                    product.getProductId(), product.getName(), product.getDescription(), product.getStatus(),
                    CompanyResponse.Simple.from(product.getCompany()), hub, CategoryResponse.Simple.from(product.getCategory()),
                    product.getPrice(), product.getQuantity()
            );
        }
    }

    public record Summary(@JsonUnwrapped Info info, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Summary of(Product product, HubResponse hub) {
            return new Summary(Info.of(product, hub), product.getCreatedAt(), product.getUpdatedAt());
        }
    }

    public record Detail(
            @JsonUnwrapped Info info, LocalDateTime createdAt, LocalDateTime updatedAt,
            UserResponse createdBy, UserResponse updatedBy
    ) {
        public static Detail of(Product product, HubResponse hub, UserResponse createdBy, UserResponse updatedBy) {
            return new Detail(Info.of(product, hub), product.getCreatedAt(), product.getUpdatedAt(), createdBy, updatedBy);
        }
    }

    public record Update(
            UUID productId, String name, String description,
            CategoryResponse.Simple category, Long price, LocalDateTime updatedAt
    ) {
        public static Update from(Product product) {
            return new Update(
                    product.getProductId(), product.getName(), product.getDescription(),
                    CategoryResponse.Simple.from(product.getCategory()), product.getPrice(), product.getUpdatedAt()
            );
        }
    }

    public record Delete(UUID productId, String name, LocalDateTime deletedAt) {
        public static Delete from(Product product) {
            return new Delete(product.getProductId(), product.getName(), product.getDeletedAt());
        }
    }
}
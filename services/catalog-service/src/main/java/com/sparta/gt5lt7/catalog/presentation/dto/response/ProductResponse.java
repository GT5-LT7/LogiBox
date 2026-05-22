package com.sparta.gt5lt7.catalog.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.domain.entity.ProductStatus;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Info(
            UUID productId, String name, String description, ProductStatus status,
            CompanyResponse.Simple company, HubResponse hub, CategoryResponse.Simple category, Long price, Integer quantity
    ) {
        public static ProductResponse.Info from(Product product) {
            return new ProductResponse.Info(
                    product.getProductId(), product.getName(), product.getDescription(), null,
                    null, null, CategoryResponse.Simple.from(product.getCategory()),
                    product.getPrice(), null
            );
        }

        public static ProductResponse.Info of(Product product, HubResponse hub) {
            return new ProductResponse.Info(
                    product.getProductId(), product.getName(), product.getDescription(), product.getStatus(),
                    CompanyResponse.Simple.from(product.getCompany()), hub, CategoryResponse.Simple.from(product.getCategory()),
                    product.getPrice(), product.getQuantity()
            );
        }
    }

    public record Create(Info info, LocalDateTime createdAt) {
        public static Create from(Product product) {
            return new Create(Info.from(product), product.getCreatedAt());
        }
    }

    public record Summary(Info info, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Summary of(Product product, HubResponse hub) {
            return new Summary(Info.of(product, hub), product.getCreatedAt(), product.getUpdatedAt());
        }
    }

    public record Update(Info info, LocalDateTime updatedAt) {
        public static Update from(Product product) {
            return new Update(Info.from(product), product.getUpdatedAt());
        }
    }

    public record Delete(UUID productId, String name, LocalDateTime deletedAt) {
        public static Delete from(Product product) {
            return new Delete(product.getProductId(), product.getName(), product.getDeletedAt());
        }
    }
}
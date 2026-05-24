package com.sparta.gt5lt7.order.infrastructure.client;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CatalogClient {

    public void validateProductExists(UUID productId) {
        // TODO: Catalog Service API 호출
    }

    public void decreaseStock(UUID productId, Integer quantity) {
        // TODO: Catalog Service API 호출
    }

    public void restoreStock(UUID productId, Integer quantity) {

        // TODO: Catalog Service 재고 복구 API 호출
    }

    public void validateStock(UUID productId, Integer quantity) {
        // TODO: Catalog Service 재고 검증 API 호출
    }
}

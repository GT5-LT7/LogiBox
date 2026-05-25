package com.sparta.gt5lt7.catalog.domain.repository;

import com.sparta.gt5lt7.catalog.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductRepositoryCustom {
    Page<Product> searchProducts(
            String keyword, Boolean salesOnly, UUID companyId, UUID hubId, UUID categoryId,
            Pageable pageable, com.sparta.gt5lt7.common.security.CustomUserPrincipal principal
    );
}
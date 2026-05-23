package com.sparta.gt5lt7.catalog.domain.repository;

import com.sparta.gt5lt7.catalog.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductRepositoryCustom {
    Page<Product> searchProducts(String keyword, UUID companyId, UUID hubId, UUID categoryId, Pageable pageable);
}
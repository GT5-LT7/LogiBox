package com.sparta.gt5lt7.catalog.domain.repository;

import com.sparta.gt5lt7.catalog.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query("SELECT p FROM Product p JOIN FETCH p.company WHERE p.productId = :productId")
    Optional<Product> findByIdWithCompany(@Param("productId") UUID productId);
}
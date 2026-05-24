package com.sparta.gt5lt7.catalog.domain.repository;

import com.sparta.gt5lt7.catalog.domain.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, ProductRepositoryCustom {
    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.company LEFT JOIN FETCH p.category
        WHERE p.productId = :productId
    """)
    Optional<Product> findByIdWithCompanyAndCategory(@Param("productId") UUID productId);

    @Modifying
    @Query("""
        UPDATE Product p
        SET p.deletedAt = :now, p.deletedBy = :deletedBy
        WHERE p.company.companyId = :companyId AND p.deletedAt IS NULL
    """)
    void softDeleteByProductId(
            @Param("companyId") UUID companyId, @Param("deletedBy") UUID deletedBy, @Param("now") LocalDateTime now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId IN :ids")
    List<Product> findAllByIdInForUpdate(@Param("ids") List<UUID> ids);
}
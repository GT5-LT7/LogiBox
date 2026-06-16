package com.sparta.gt5lt7.catalog.domain.entity;

import com.sparta.gt5lt7.catalog.presentation.dto.request.ActionType;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "p_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "company_id")
    private Company company;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer quantity;

    @Version
    private Long version;

    @Builder
    public Product(String name, String description, Company company, Long price, Integer quantity) {
        this.name = name.trim();
        this.description = (description != null && !description.isBlank()) ? description.trim() : null;
        this.status = (quantity > 0) ? ProductStatus.ON_SALE : ProductStatus.SOLD_OUT;
        this.company = company;
        this.price = price;
        this.quantity = quantity;
    }

    public void update(ProductRequest.Update request) {
        String description = request.description();

        this.name = request.name().trim();
        this.description = (description != null && !description.isBlank()) ? description.trim() : null;
        this.price = request.price();
    }
    
    public void updateStatus(ActionType action) {
        this.status = switch (action) {
            case SHOW -> (this.quantity > 0) ? ProductStatus.ON_SALE : ProductStatus.SOLD_OUT;
            case HIDE -> ProductStatus.HIDDEN;
            case STOP -> ProductStatus.STOPPED;
        };
    }

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public boolean isHidden() {
        return this.status == ProductStatus.HIDDEN;
    }
}
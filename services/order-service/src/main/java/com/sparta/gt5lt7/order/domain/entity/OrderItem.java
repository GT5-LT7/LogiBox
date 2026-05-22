package com.sparta.gt5lt7.order.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_item_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "menu_id", nullable = false)
    private UUID menuId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    private OrderItem(Order order, UUID menuId, Integer quantity, Integer unitPrice, String createdBy) {
        this.order = order;
        this.menuId = menuId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
    }

    public static OrderItem create(
            Order order,
            UUID menuId,
            Integer quantity,
            Integer unitPrice,
            String createdBy
    ) {
        return new OrderItem(order, menuId, quantity, unitPrice, createdBy);
    }
}
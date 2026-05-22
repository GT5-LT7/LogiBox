package com.sparta.gt5lt7.order.domain.entity;

import com.sparta.gt5lt7.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "customer_id", nullable = false, length = 10)
    private String customerId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "address_id")
    private UUID addressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "request", columnDefinition = "TEXT")
    private String request;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(
            String customerId,
            UUID storeId,
            UUID addressId,
            Integer totalPrice,
            String request
    ) {
        this.customerId = customerId;
        this.storeId = storeId;
        this.addressId = addressId;
        this.status = OrderStatus.PENDING;
        this.totalPrice = totalPrice;
        this.request = request;
    }

    public static Order create(
            String customerId,
            UUID storeId,
            UUID addressId,
            Integer totalPrice,
            String request
    ) {
        return new Order(
                customerId,
                storeId,
                addressId,
                totalPrice,
                request
        );
    }

    public void updateRequest(String request) {
        this.request = request;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
    }

    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }
}

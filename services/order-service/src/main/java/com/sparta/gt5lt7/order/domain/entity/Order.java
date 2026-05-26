package com.sparta.gt5lt7.order.domain.entity;

import com.sparta.gt5lt7.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    @Column(name = "supplier_company_id", nullable = false)
    private UUID supplierCompanyId;

    @Column(name = "receiver_company_id", nullable = false)
    private UUID receiverCompanyId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "request_message", length = 255)
    private String requestMessage;

    @Column(name = "delivery_deadline")
    private LocalDateTime deliveryDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Column(name = "ai_log_id", nullable = false)
    private UUID aiLogId;

    @Column(name = "slack_id", nullable = false)
    private UUID slackId;

    @Builder
    private Order(
            UUID supplierCompanyId,
            UUID receiverCompanyId,
            UUID productId,
            Integer quantity,
            UUID deliveryId,
            String requestMessage,
            LocalDateTime deliveryDeadline,
            UUID aiLogId,
            UUID slackId
    ) {
        this.supplierCompanyId = supplierCompanyId;
        this.receiverCompanyId = receiverCompanyId;
        this.productId = productId;
        this.quantity = quantity;
        this.deliveryId = deliveryId;
        this.requestMessage = requestMessage;
        this.deliveryDeadline = deliveryDeadline;
        this.orderStatus = OrderStatus.PENDING;
        this.aiLogId = aiLogId;
        this.slackId = slackId;
    }

    public void updateOrder(
            Integer quantity,
            String requestMessage,
            LocalDateTime deliveryDeadline
    ) {
        this.quantity = quantity;
        this.requestMessage = requestMessage;
        this.deliveryDeadline = deliveryDeadline;
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void assignDelivery(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public void waitingForDelivery() {
        this.orderStatus = OrderStatus.WAITING_FOR_DELIVERY;
    }

    public void shipping() {
        this.orderStatus = OrderStatus.SHIPPING;
    }

    public void complete() {
        this.orderStatus = OrderStatus.COMPLETED;
    }

    public void fail() {
        this.orderStatus = OrderStatus.FAILED;
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELED;
    }
}
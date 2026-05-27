package com.sparta.gt5lt7.logisticsservice.domain.entity;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "p_deliveries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "from_hub_id", nullable = false)
    private UUID fromHubId;

    @Column(name = "to_hub_id", nullable = false)
    private UUID toHubId;

    @Column(name = "receiver_company_id", nullable = false)
    private UUID receiverCompanyId;

    @Column(name = "receiver_address", nullable = false, length = 225)
    private String receiverAddress;

    @Column(name = "delivery_agent_id")
    private UUID deliveryAgentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 30)
    @Builder.Default
    private DeliveryStatus deliveryStatus = DeliveryStatus.READY;

    @Column(name = "request_message", length = 225)
    private String requestMessage;

    @Column(name = "departed_at")
    private LocalDateTime departedAt;

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "ai_log_id", nullable = false)
    private UUID aiLogId;

    @Column(name = "slack_id", nullable = false)
    private UUID slackId;

    @OneToMany(
            mappedBy = "delivery",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<DeliveryRoute> routes = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.deliveryStatus == null) {
            this.deliveryStatus = DeliveryStatus.READY;
        }
    }

    public void assignDeliveryAgent(UUID deliveryAgentId) {
        this.deliveryAgentId = deliveryAgentId;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(DeliveryStatus status) {
        Objects.requireNonNull(status, "주문란은 비워질 수 없습니다.");
        this.deliveryStatus = status;
        this.updatedAt = LocalDateTime.now();

        if (status == DeliveryStatus.IN_TRANSIT) {
            this.departedAt = LocalDateTime.now();
        }

        if (status == DeliveryStatus.ARRIVED_HUB) {
            this.arrivedAt = LocalDateTime.now();
        }

        if (status == DeliveryStatus.DELIVERED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public void addRoute(DeliveryRoute route) {
        this.routes.add(route);
    }

    public void softDelete(UUID deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}
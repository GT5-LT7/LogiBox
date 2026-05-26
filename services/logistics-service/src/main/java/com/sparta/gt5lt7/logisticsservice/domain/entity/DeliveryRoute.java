package com.sparta.gt5lt7.logisticsservice.domain.entity;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryRouteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_delivery_routes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeliveryRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_route_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "from_hub_id", nullable = false)
    private UUID fromHubId;

    @Column(name = "to_hub_id", nullable = false)
    private UUID toHubId;

    @Column(name = "estimated_distance", nullable = false)
    private Double estimatedDistance;

    @Column(name = "estimated_duration", nullable = false)
    private Integer estimatedDuration;

    @Column(name = "actual_distance")
    private Double actualDistance;

    @Column(name = "actual_duration")
    private Integer actualDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_route_status", nullable = false, length = 30)
    @Builder.Default
    private DeliveryRouteStatus deliveryRouteStatus = DeliveryRouteStatus.READY;

    @Column(name = "hub_delivery_agent_id", nullable = false)
    private UUID hubDeliveryAgentId;

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

        if (this.deliveryRouteStatus == null) {
            this.deliveryRouteStatus = DeliveryRouteStatus.READY;
        }
    }

    public void assignHubDeliveryAgent(UUID hubDeliveryAgentId) {
        this.hubDeliveryAgentId = hubDeliveryAgentId;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(DeliveryRouteStatus status) {
        this.deliveryRouteStatus = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void completeRoute(Double actualDistance, Integer actualDuration) {
        this.actualDistance = actualDistance;
        this.actualDuration = actualDuration;
        this.deliveryRouteStatus = DeliveryRouteStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete(UUID deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}

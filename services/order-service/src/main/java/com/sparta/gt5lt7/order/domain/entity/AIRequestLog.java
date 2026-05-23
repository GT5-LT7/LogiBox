package com.sparta.gt5lt7.order.domain.entity;

import com.sparta.gt5lt7.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_ai_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AIRequestLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_log_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "slack_id", nullable = false)
    private UUID slackId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_type", nullable = false)
    private AIType aiType;

    @Column(name = "request_payload", nullable = false, columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "final_dispatch_deadline")
    private java.time.LocalDateTime finalDispatchDeadline;

    @Column(name = "estimated_time_minutes")
    private Integer estimatedTimeMinutes;

    @Column(name = "estimated_cost")
    private Integer estimatedCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 30)
    private RiskLevel riskLevel;

    @Builder
    private AIRequestLog(
            UUID deliveryId,
            UUID orderId,
            UUID slackId,
            AIType aiType,
            String requestPayload
    ) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.slackId = slackId;
        this.aiType = aiType;
        this.requestPayload = requestPayload;
    }

    public void updateResponse(
            String responsePayload,
            java.time.LocalDateTime finalDispatchDeadline,
            Integer estimatedTimeMinutes,
            Integer estimatedCost,
            RiskLevel riskLevel
    ) {
        this.responsePayload = responsePayload;
        this.finalDispatchDeadline = finalDispatchDeadline;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.estimatedCost = estimatedCost;
        this.riskLevel = riskLevel;
    }
}

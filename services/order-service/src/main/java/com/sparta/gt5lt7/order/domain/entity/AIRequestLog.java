package com.sparta.gt5lt7.order.domain.entity;

import com.sparta.gt5lt7.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_ai_request_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AIRequestLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_request_log_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "request_prompt", nullable = false, columnDefinition = "TEXT")
    private String requestPrompt;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Builder
    private AIRequestLog(
            UUID orderId,
            String requestPrompt
    ) {
        this.orderId = orderId;
        this.requestPrompt = requestPrompt;
        this.success = false;
        this.requestedAt = LocalDateTime.now();
    }

    public void success(String responseText) {
        this.responseText = responseText;
        this.success = true;
        this.respondedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
        this.respondedAt = LocalDateTime.now();
    }
}

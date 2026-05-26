package com.sparta.gt5lt7.logisticsservice.domain.entity;

import com.sparta.gt5lt7.logisticsservice.domain.entity.type.AgentType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "p_delivery_agents",
        indexes = {
                @Index(name = "idx_delivery_agents_hub", columnList = "hub_id"),
                @Index(name = "idx_delivery_agents_type", columnList = "agent_type"),
                @Index(name = "idx_delivery_agents_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_delivery_agents_user",
                        columnNames = "user_id"
                ),
                @UniqueConstraint(
                        name = "uk_delivery_agents_sequence_per_scope",
                        columnNames = {"agent_type", "hub_id", "delivery_sequence"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeliveryAgent extends com.sparta.gt5lt7.common.entity.BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_agent_id")
    private UUID deliveryAgentId;

    @Column(name = "hub_id")
    private UUID hubId;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "slack_user_id", nullable = false, length = 100)
    private String slackUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false)
    private AgentType agentType;

    @Column(name = "delivery_sequence", nullable = false)
    private Integer deliverySequence;

    public static DeliveryAgent create(
            UUID userId,
            UUID hubId,
            String slackUserId,
            AgentType agentType,
            Integer deliverySequence
    ) {
        return DeliveryAgent.builder()
                .userId(userId)
                .hubId(hubId)
                .slackUserId(slackUserId)
                .agentType(agentType)
                .deliverySequence(deliverySequence)
                .build();
    }

    // 소속 허브 변경 + 새 시퀀스 부여, COMPANY 타입에서만 호출됨 (Service에서 보장).
    public void updateHubId(UUID newHubId, Integer newSequence) {
        this.hubId = newHubId;
        this.deliverySequence = newSequence;
    }

    public void updateSlackUserId(String slackUserId) {
        this.slackUserId = slackUserId;
    }
}
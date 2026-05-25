package com.sparta.gt5lt7.logisticsservice.domain.entity;

import com.sparta.gt5lt7.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "p_hub_routes",
        indexes = {
                @Index(name = "idx_hub_routes_from_to", columnList = "from_hub_id, to_hub_id"),
                @Index(name = "idx_hub_routes_from", columnList = "from_hub_id"),
                @Index(name = "idx_hub_routes_to", columnList = "to_hub_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HubRoute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "route_id")
    private UUID routeId;

    @Column(name = "from_hub_id", nullable = false)
    private UUID fromHubId;

    @Column(name = "to_hub_id", nullable = false)
    private UUID toHubId;

    @Column(name = "distance", nullable = false)
    private Integer distance;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    public static HubRoute create(UUID fromHubId, UUID toHubId, Integer distance, Integer duration) {
        return HubRoute.builder()
                .fromHubId(fromHubId)
                .toHubId(toHubId)
                .distance(distance)
                .duration(duration)
                .build();
    }
}
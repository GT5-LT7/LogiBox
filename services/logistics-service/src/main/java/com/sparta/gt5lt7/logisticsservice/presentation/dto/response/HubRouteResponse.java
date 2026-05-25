package com.sparta.gt5lt7.logisticsservice.presentation.dto.response;

import com.sparta.gt5lt7.logisticsservice.domain.entity.HubRoute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubRouteResponse implements Serializable {

    private UUID routeId;
    private UUID fromHubId;
    private UUID toHubId;
    private Integer distance;
    private Integer duration;

    public static HubRouteResponse from(HubRoute route) {
        return HubRouteResponse.builder()
                .routeId(route.getRouteId())
                .fromHubId(route.getFromHubId())
                .toHubId(route.getToHubId())
                .distance(route.getDistance())
                .duration(route.getDuration())
                .build();
    }
}
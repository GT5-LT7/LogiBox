package com.sparta.gt5lt7.logisticsservice.presentation.dto.response;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class HubResponse implements Serializable {

    private UUID hubId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private String createdBy;

    /**
     * Create a HubResponse DTO populated from the given domain Hub entity.
     *
     * @param hub the source Hub entity whose properties will be copied into the response
     * @return a HubResponse containing values copied from the provided Hub
     */
    public static HubResponse from(Hub hub) {
        return HubResponse.builder()
                .hubId(hub.getHubId())
                .name(hub.getName())
                .address(hub.getAddress())
                .latitude(hub.getLatitude())
                .longitude(hub.getLongitude())
                .createdAt(hub.getCreatedAt())
                .createdBy(hub.getCreatedBy())
                .build();
    }
}
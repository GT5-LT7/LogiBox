package com.sparta.gt5lt7.logisticsservice.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HubRouteSearchRequest {
    private UUID fromHubId;
    private UUID toHubId;
}
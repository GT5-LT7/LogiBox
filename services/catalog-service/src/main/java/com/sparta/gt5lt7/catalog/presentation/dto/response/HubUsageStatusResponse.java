package com.sparta.gt5lt7.catalog.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HubUsageStatusResponse {
    private boolean used;
}
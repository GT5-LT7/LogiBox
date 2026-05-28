package com.sparta.gt5lt7.catalog.infrastructure.client.dto;

import java.util.UUID;

public record HubResponse(UUID hubId, String name) {
    public UUID id() {
        return this.hubId;
    }
}
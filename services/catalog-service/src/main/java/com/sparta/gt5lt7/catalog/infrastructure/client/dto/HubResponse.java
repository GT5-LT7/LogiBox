package com.sparta.gt5lt7.catalog.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class HubResponse {
    private UUID id;
    private String name;
}
package com.sparta.gt5lt7.catalog.infrastructure.client.dto;

import java.util.List;

public record KakaoMapResponse(List<Document> documents) {
    public record Document(
            String x, // 경도
            String y  // 위도
    ) {}
}
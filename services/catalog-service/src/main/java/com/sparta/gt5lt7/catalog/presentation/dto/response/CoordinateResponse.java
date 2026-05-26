package com.sparta.gt5lt7.catalog.presentation.dto.response;

import com.sparta.gt5lt7.catalog.infrastructure.client.dto.KakaoMapResponse;

import java.math.BigDecimal;

public record CoordinateResponse(BigDecimal latitude, BigDecimal longitude) {
    public static CoordinateResponse from(KakaoMapResponse.Document document) {
        return new CoordinateResponse(new BigDecimal(document.y()), new BigDecimal(document.x()));
    }
}
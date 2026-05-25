package com.sparta.gt5lt7.catalog.application.service;

import com.sparta.gt5lt7.catalog.presentation.dto.response.CoordinateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WithMockUser
@SpringBootTest
class KakaoMapServiceIntegrationTest {
    @Autowired
    private KakaoMapService kakaoMapService;

    @Test
    @DisplayName("좌표 변환 테스트")
    void GetCoordinatesTest() {
        // Given
        String address = "서울시 강남구 테헤란로 311";

        // When
        CoordinateResponse result = kakaoMapService.getCoordinates(address);

        // Then
        assertThat(result.latitude()).isBetween(new BigDecimal("37.0"), new BigDecimal("37.8"));
        assertThat(result.longitude()).isBetween(new BigDecimal("126.7"), new BigDecimal("127.3"));
    }
}
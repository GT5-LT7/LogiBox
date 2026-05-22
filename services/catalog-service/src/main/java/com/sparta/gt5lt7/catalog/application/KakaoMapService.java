package com.sparta.gt5lt7.catalog.application;

import com.sparta.gt5lt7.catalog.global.exception.KakaoMapErrorCode;
import com.sparta.gt5lt7.catalog.infrastructure.client.KakaoMapClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.KakaoMapResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CoordinateResponse;
import com.sparta.gt5lt7.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoMapService {
    private final KakaoMapClient kakaoMapClient;

    @Value("${kakao.map.auth-header}")
    private String kakaoAuthHeader;

    public CoordinateResponse getCoordinates(String address) {
        KakaoMapResponse response = kakaoMapClient.searchCoordinates(address.trim(), kakaoAuthHeader);

        if (response.documents() == null || response.documents().isEmpty()) {
            throw new BaseException(KakaoMapErrorCode.ADDRESS_NOT_FOUND);
        }

        KakaoMapResponse.Document document = response.documents().get(0);
        return CoordinateResponse.from(document);
    }
}
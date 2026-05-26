package com.sparta.gt5lt7.logisticsservice.infrastructure.client.kakao;

import com.sparta.gt5lt7.logisticsservice.infrastructure.client.kakao.dto.KakaoDirectionsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "kakaoMobilityClient",
        url = "${kakao.mobility.base-url}",
        configuration = KakaoFeignConfig.class
)
public interface KakaoMobilityClient {

    // 카카오모빌리티 길찾기 API. origin/destination 좌표 포맷: "경도,위도" (lng,lat).
    @GetMapping("/v1/directions")
    KakaoDirectionsResponse getDirections(
            @RequestParam("origin") String origin,
            @RequestParam("destination") String destination
    );
}

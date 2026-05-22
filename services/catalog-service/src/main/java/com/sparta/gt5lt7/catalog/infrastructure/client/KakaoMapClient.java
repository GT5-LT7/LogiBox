package com.sparta.gt5lt7.catalog.infrastructure.client;

import com.sparta.gt5lt7.catalog.infrastructure.client.dto.KakaoMapResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kakao-map-client", url = "https://dapi.kakao.com")
public interface KakaoMapClient {
    @GetMapping("/v2/local/search/address.json")
    KakaoMapResponse searchCoordinates(@RequestParam("query") String query, @RequestHeader("Authorization") String authorization);
}
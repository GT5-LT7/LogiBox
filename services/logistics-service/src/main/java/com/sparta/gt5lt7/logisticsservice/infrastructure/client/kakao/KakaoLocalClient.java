package com.sparta.gt5lt7.logisticsservice.infrastructure.client.kakao;

import com.sparta.gt5lt7.logisticsservice.infrastructure.client.kakao.dto.KakaoLocalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "kakaoLocalClient",
        url = "${kakao.local.base-url}",
        configuration = KakaoFeignConfig.class
)
public interface KakaoLocalClient {

    @GetMapping("/v2/local/search/address.json")
    KakaoLocalResponse searchAddress(@RequestParam("query") String query);
}
package com.sparta.gt5lt7.logisticsservice.infrastructure.client.kakao;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KakaoFeignConfig {

    @Value("${kakao.local.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor kakaoAuthInterceptor() {
        return template -> template.header("Authorization", "KakaoAK " + apiKey);
    }
}
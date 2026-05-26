package com.sparta.gt5lt7.common.config;

import com.sparta.gt5lt7.common.security.InternalFeignHeaderInterceptor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class InternalFeignConfig {
    @Bean
    public RequestInterceptor internalFeignHeaderInterceptor() {
        return new InternalFeignHeaderInterceptor();
    }
}
package com.sparta.gt5lt7.logisticsservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "kakao.local.api-key=test-kakao-api-key",
        "redis.host=localhost",
        "eureka.client.enabled=false"
})
class LogisticsServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}

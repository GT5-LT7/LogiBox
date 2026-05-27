package com.sparta.gt5lt7.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "gemini.api.key=test-api-key",
        "gemini.model=gemini-2.0-flash",
        "slack.webhook.url=https://test.slack.webhook",
        "eureka.client.enabled=false"
})
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
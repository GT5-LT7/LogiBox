package com.sparta.gt5lt7.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "gemini.api.key=test-api-key",
        "gemini.model=gemini-2.0-flash",
        "slack.webhook.url=https://test.slack.webhook"
})
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
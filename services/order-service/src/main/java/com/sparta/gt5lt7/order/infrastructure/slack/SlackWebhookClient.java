package com.sparta.gt5lt7.order.infrastructure.slack;

import com.sparta.gt5lt7.order.infrastructure.slack.dto.SlackMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class SlackWebhookClient {

    private final RestClient restClient = RestClient.create();

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    public void send(String message) {

        SlackMessageRequest request = new SlackMessageRequest(message);

        restClient.post()
                .uri(webhookUrl)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
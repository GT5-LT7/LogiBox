package com.sparta.gt5lt7.order.application.service;

import com.sparta.gt5lt7.order.domain.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.sparta.gt5lt7.order.infrastructure.slack.SlackWebhookClient;

@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    private final SlackWebhookClient slackWebhookClient;

    public void sendOrderCreated(Order order) {

        String message = """
                📦 주문 생성
                
                주문 ID: %s
                상품 ID: %s
                수량: %d
                주문 상태: %s
                """.formatted(
                order.getId(),
                order.getProductId(),
                order.getQuantity(),
                order.getOrderStatus()
        );

        slackWebhookClient.send(message);
    }

    public void sendDeliveryStarted(Order order) {

        String message = """
                🚚 배송 시작
                
                주문 ID: %s
                배송이 시작되었습니다.
                """.formatted(
                order.getId()
        );

        slackWebhookClient.send(message);
    }

    public void sendDeliveryFailed(Order order) {

        String message = """
                ❌ 배송 실패
                
                주문 ID: %s
                배송 생성에 실패했습니다.
                """.formatted(
                order.getId()
        );

        slackWebhookClient.send(message);
    }

    public void sendDeliveryCompleted(Order order) {

        String message = """
                ✅ 배송 완료
                
                주문 ID: %s
                배송이 완료되었습니다.
                """.formatted(
                order.getId()
        );

        slackWebhookClient.send(message);
    }

    public void sendUrgentDelivery(Order order) {

        String message = """
                🚨 긴급 배송
                
                주문 ID: %s
                긴급 배송 대상 주문입니다.
                """.formatted(
                order.getId()
        );

        slackWebhookClient.send(message);
    }
}
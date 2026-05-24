package com.sparta.gt5lt7.order.application.service;

import com.sparta.gt5lt7.order.domain.entity.Order;
import com.sparta.gt5lt7.order.domain.entity.OrderHistory;
import com.sparta.gt5lt7.order.domain.entity.OrderStatus;
import com.sparta.gt5lt7.order.domain.repository.OrderHistoryRepository;
import com.sparta.gt5lt7.order.domain.repository.OrderRepository;
import com.sparta.gt5lt7.order.infrastructure.client.CatalogClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderSagaService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final CatalogClient catalogClient;
    private final SlackNotificationService slackNotificationService;

    @Transactional
    public void handleDeliveryCreated(UUID orderId, UUID deliveryId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow();

        order.assignDelivery(deliveryId);

        order.waitingForDelivery();

        saveHistory(
                orderId,
                OrderStatus.PENDING,
                OrderStatus.WAITING_FOR_DELIVERY,
                "배송 생성 완료"
        );
    }

    @Transactional
    public void handleDeliveryStarted(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow();

        order.shipping();

        saveHistory(
                orderId,
                OrderStatus.WAITING_FOR_DELIVERY,
                OrderStatus.SHIPPING,
                "배송 시작"
        );

        slackNotificationService.sendDeliveryStarted(order);
    }

    @Transactional
    public void handleDeliveryCompleted(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow();

        order.shipping();

        saveHistory(
                orderId,
                OrderStatus.SHIPPING,
                OrderStatus.COMPLETED,
                "배송 완료"
        );

        slackNotificationService.sendDeliveryCompleted(order);
    }

    @Transactional
    public void handleDeliveryFailed(UUID orderId, String reason) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow();

        catalogClient.restoreStock(
                order.getProductId(),
                order.getQuantity()
        );

        order.fail();

        saveHistory(
                orderId,
                OrderStatus.PENDING,
                OrderStatus.FAILED,
                reason
        );

        slackNotificationService.sendDeliveryFailed(order);
    }

    private void saveHistory(
            UUID orderId,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String reason
    ) {

        OrderHistory history = OrderHistory.builder()
                .orderId(orderId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build();

        orderHistoryRepository.save(history);
    }
}

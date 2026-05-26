package com.sparta.gt5lt7.order.application.service;

import com.sparta.gt5lt7.order.domain.entity.Order;
import com.sparta.gt5lt7.order.domain.repository.OrderHistoryRepository;
import com.sparta.gt5lt7.order.domain.repository.OrderRepository;
import com.sparta.gt5lt7.order.infrastructure.client.CatalogClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSagaServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderHistoryRepository orderHistoryRepository;
    @Mock CatalogClient catalogClient;
    @Mock SlackNotificationService slackNotificationService;

    @InjectMocks OrderSagaService orderSagaService;

    @Test
    void 배송_생성_성공시_WAITING_FOR_DELIVERY_변경() {
        UUID orderId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();

        Order order = mock(Order.class);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderSagaService.handleDeliveryCreated(orderId, deliveryId);

        verify(order).assignDelivery(deliveryId);
        verify(order).waitingForDelivery();
        verify(orderHistoryRepository).save(any());
    }

    @Test
    void 배송_시작시_SHIPPING_변경() {
        UUID orderId = UUID.randomUUID();

        Order order = mock(Order.class);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderSagaService.handleDeliveryStarted(orderId);

        verify(order).shipping();
        verify(orderHistoryRepository).save(any());
        verify(slackNotificationService).sendDeliveryStarted(order);
    }

    @Test
    void 배송_생성_실패시_재고복구_및_FAILED_변경() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Order order = mock(Order.class);
        when(order.getProductId()).thenReturn(productId);
        when(order.getQuantity()).thenReturn(5);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderSagaService.handleDeliveryFailed(orderId, "배송 생성 실패");

        verify(catalogClient).restoreStock(productId, 5);
        verify(order).fail();
        verify(orderHistoryRepository).save(any());
        verify(slackNotificationService).sendDeliveryFailed(order);
    }
}
package com.sparta.gt5lt7.order.application.service;

import com.sparta.gt5lt7.order.domain.entity.Order;
import com.sparta.gt5lt7.order.domain.entity.OrderStatus;
import com.sparta.gt5lt7.order.domain.repository.OrderHistoryRepository;
import com.sparta.gt5lt7.order.domain.repository.OrderRepository;
import com.sparta.gt5lt7.order.infrastructure.client.CatalogClient;
import com.sparta.gt5lt7.order.infrastructure.client.LogisticsClient;
import com.sparta.gt5lt7.order.infrastructure.lock.RedisLockService;
import com.sparta.gt5lt7.order.infrastructure.messaging.OrderEventProducer;
import com.sparta.gt5lt7.order.presentation.dto.request.OrderCreateRequest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderHistoryRepository orderHistoryRepository;
    @Mock CatalogClient catalogClient;
    @Mock LogisticsClient logisticsClient;
    @Mock RedisLockService redisLockService;
    @Mock OrderEventProducer orderEventProducer;
    @Mock SlackNotificationService slackNotificationService;

    @InjectMocks OrderService orderService;

    @Test
    void 주문_생성_성공() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderCreateRequest request = new OrderCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                productId,
                3,
                "문 앞 배송",
                LocalDateTime.now().plusDays(1)
        );

        when(redisLockService.tryLock(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createOrder(request, userId);

        verify(catalogClient).validateProductExists(productId);
        verify(catalogClient).decreaseStock(productId, 3);
        verify(orderHistoryRepository).save(any());
        verify(orderEventProducer).publishOrderCreated(any());
        verify(redisLockService).unlock(anyString(), anyString());
    }

    @Test
    void 재고_부족_주문_실패() {
        UUID userId = UUID.randomUUID();

        OrderCreateRequest request = new OrderCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                10,
                null,
                LocalDateTime.now().plusDays(1)
        );

        when(redisLockService.tryLock(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        doThrow(new RuntimeException("재고 부족"))
                .when(catalogClient)
                .decreaseStock(any(UUID.class), anyInt());

        try {
            orderService.createOrder(request, userId);
        } catch (Exception ignored) {
        }

        verify(orderRepository, never()).save(any());
        verify(redisLockService).unlock(anyString(), anyString());
    }

    @Test
    void Redis_락_획득_실패시_주문_실패() {
        OrderCreateRequest request = new OrderCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                null,
                LocalDateTime.now().plusDays(1)
        );

        when(redisLockService.tryLock(anyString(), anyString(), any(Duration.class)))
                .thenReturn(false);

        try {
            orderService.createOrder(request, UUID.randomUUID());
        } catch (Exception ignored) {
        }

        verify(orderRepository, never()).save(any());
    }
}
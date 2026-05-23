package com.sparta.gt5lt7.order.application.service;

import com.sparta.gt5lt7.order.application.event.OrderCreatedEvent;
import com.sparta.gt5lt7.order.domain.entity.Order;
import com.sparta.gt5lt7.order.domain.entity.OrderHistory;
import com.sparta.gt5lt7.order.domain.entity.OrderStatus;
import com.sparta.gt5lt7.order.domain.repository.OrderHistoryRepository;
import com.sparta.gt5lt7.order.domain.repository.OrderRepository;
import com.sparta.gt5lt7.order.infrastructure.client.CatalogClient;
import com.sparta.gt5lt7.order.infrastructure.client.LogisticsClient;
import com.sparta.gt5lt7.order.infrastructure.lock.RedisLockService;
import com.sparta.gt5lt7.order.infrastructure.messaging.OrderEventProducer;
import com.sparta.gt5lt7.order.presentation.dto.request.OrderCreateRequest;
import com.sparta.gt5lt7.order.presentation.dto.response.OrderCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final CatalogClient catalogClient;
    private final LogisticsClient logisticsClient;
    private final RedisLockService redisLockService;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, UUID userId) {
        String lockKey = "lock:stock:" + request.productId();
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLockService.tryLock(lockKey, lockValue, Duration.ofSeconds(5));

        if (!locked) {
            throw new IllegalStateException("현재 주문 요청이 많습니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            catalogClient.validateProductExists(request.productId());

            logisticsClient.validateHubExists(request.supplierCompanyId());
            logisticsClient.validateHubExists(request.receiverCompanyId());

            catalogClient.decreaseStock(request.productId(), request.quantity());

            Order order = Order.builder()
                    .supplierCompanyId(request.supplierCompanyId())
                    .receiverCompanyId(request.receiverCompanyId())
                    .productId(request.productId())
                    .quantity(request.quantity())
                    .requestMessage(request.requestMessage())
                    .deliveryDeadline(request.deliveryDeadline())
                    .aiLogId(UUID.randomUUID())
                    .slackId(UUID.randomUUID())
                    .build();

            Order savedOrder = orderRepository.save(order);

            OrderHistory history = OrderHistory.builder()
                    .orderId(savedOrder.getId())
                    .previousStatus(null)
                    .newStatus(OrderStatus.PENDING)
                    .reason("주문 생성")
                    .build();

            orderHistoryRepository.save(history);

            orderEventProducer.publishOrderCreated(
                    new OrderCreatedEvent(
                            savedOrder.getId(),
                            savedOrder.getSupplierCompanyId(),
                            savedOrder.getReceiverCompanyId(),
                            savedOrder.getProductId(),
                            savedOrder.getQuantity(),
                            savedOrder.getDeliveryDeadline(),
                            savedOrder.getRequestMessage()
                    )
            );

            return new OrderCreateResponse(
                    savedOrder.getId(),
                    savedOrder.getSupplierCompanyId(),
                    savedOrder.getReceiverCompanyId(),
                    savedOrder.getProductId(),
                    savedOrder.getQuantity(),
                    savedOrder.getOrderStatus(),
                    savedOrder.getDeliveryDeadline()
            );

        } finally {
            redisLockService.unlock(lockKey, lockValue);
        }
    }
}
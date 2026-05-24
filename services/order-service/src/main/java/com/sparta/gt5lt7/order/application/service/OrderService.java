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

import com.sparta.gt5lt7.order.presentation.dto.request.OrderSearchCondition;
import com.sparta.gt5lt7.order.presentation.dto.response.OrderResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final CatalogClient catalogClient;
    private final LogisticsClient logisticsClient;
    private final RedisLockService redisLockService;
    private final OrderEventProducer orderEventProducer;
    private final SlackNotificationService slackNotificationService;

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

            slackNotificationService.sendOrderCreated(order);

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

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(
            OrderSearchCondition condition,
            Pageable pageable,
            UUID userId
    ) {
        Specification<Order> spec = createOrderSearchSpecification(condition, userId);

        return orderRepository.findAll(spec, pageable)
                .map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (hasRole("CUSTOMER")) {
            if (!order.getCreatedBy().equals(userId)) {
                throw new AccessDeniedException("본인의 주문만 조회할 수 있습니다.");
            }
        }

        return OrderResponse.from(order);
    }

    private Specification<Order> createOrderSearchSpecification(
            OrderSearchCondition condition,
            UUID userId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasRole("CUSTOMER")) {
                predicates.add(cb.equal(root.get("createdBy"), userId));
            }

            if (condition.orderStatus() != null) {
                predicates.add(cb.equal(root.get("orderStatus"), condition.orderStatus()));
            }

            if (condition.hubId() != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("supplierCompanyId"), condition.hubId()),
                        cb.equal(root.get("receiverCompanyId"), condition.hubId())
                ));
            }

            if (condition.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), condition.startDate()));
            }

            if (condition.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), condition.endDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
package com.sparta.gt5lt7.order.domain.repository;

import com.sparta.gt5lt7.order.domain.entity.OrderHistory;
import com.sparta.gt5lt7.order.domain.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, UUID> {

    List<OrderHistory> findByOrderId(UUID orderId);

    List<OrderHistory> findByNewStatus(OrderStatus newStatus);

    List<OrderHistory> findByPreviousStatus(OrderStatus previousStatus);
}
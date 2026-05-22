package com.sparta.gt5lt7.order.domain.repository;

import com.sparta.gt5lt7.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
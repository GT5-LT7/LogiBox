package com.sparta.gt5lt7.order.domain.repository;

import com.sparta.gt5lt7.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
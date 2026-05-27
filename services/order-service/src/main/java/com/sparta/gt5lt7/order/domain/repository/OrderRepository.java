package com.sparta.gt5lt7.order.domain.repository;

import com.sparta.gt5lt7.order.domain.entity.Order;
import com.sparta.gt5lt7.order.domain.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    List<Order> findBySupplierCompanyId(UUID supplierCompanyId);

    List<Order> findByReceiverCompanyId(UUID receiverCompanyId);

    List<Order> findByProductId(UUID productId);

    List<Order> findByDeliveryId(UUID deliveryId);

    List<Order> findByAiLogId(UUID aiLogId);

    List<Order> findBySlackId(UUID slackId);

    List<Order> findByOrderStatus(OrderStatus orderStatus);
}

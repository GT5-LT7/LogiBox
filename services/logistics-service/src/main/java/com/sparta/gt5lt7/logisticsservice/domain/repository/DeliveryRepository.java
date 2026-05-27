package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Delivery;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByIdAndDeletedAtIsNull(UUID deliveryId);

    Optional<Delivery> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    List<Delivery> findAllByDeletedAtIsNull();

    boolean existsByDeliveryAgentIdAndDeliveryStatusNotInAndDeletedAtIsNull(
            UUID deliveryAgentId,
            Collection<DeliveryStatus> excludedStatuses
    );
}
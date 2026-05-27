package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, UUID> {

    Optional<DeliveryRoute> findByIdAndDeletedAtIsNull(UUID routeId);

    List<DeliveryRoute> findAllByDelivery_IdAndDeletedAtIsNullOrderBySequenceAsc(
            UUID deliveryId
    );
}
package com.sparta.gt5lt7.logisticsservice.application.service;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Delivery;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryRoute;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryDeleteService {

    private final DeliveryRepository deliveryRepository;

    @Transactional
    public void deleteDelivery(UUID deliveryId, UUID deletedBy) {

        Delivery delivery = deliveryRepository
                .findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));

        delivery.softDelete(deletedBy);

        for (DeliveryRoute route : delivery.getRoutes()) {
            route.softDelete(deletedBy);
        }
    }
}
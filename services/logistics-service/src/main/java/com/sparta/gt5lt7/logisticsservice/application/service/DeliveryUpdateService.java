package com.sparta.gt5lt7.logisticsservice.application.service;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Delivery;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryStatus;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryUpdateService {

    private final DeliveryRepository deliveryRepository;

    @Transactional
    public void updateStatus(
            UUID deliveryId,
            DeliveryStatus status
    ) {
        Delivery delivery = deliveryRepository
                .findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));

        delivery.updateStatus(status);
    }
}
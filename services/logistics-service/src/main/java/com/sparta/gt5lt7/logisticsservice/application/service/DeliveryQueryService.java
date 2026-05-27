package com.sparta.gt5lt7.logisticsservice.application.service;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Delivery;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryRoute;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryRepository;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryResponse;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryQueryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryResponse getDelivery(UUID deliveryId) {

        Delivery delivery = deliveryRepository
                .findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));

        return new DeliveryResponse(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getFromHubId(),
                delivery.getToHubId(),
                delivery.getReceiverCompanyId(),
                delivery.getReceiverAddress(),
                delivery.getDeliveryAgentId(),
                delivery.getDeliveryStatus(),
                delivery.getRequestMessage(),
                delivery.getCreatedAt()
        );
    }

    public List<DeliveryRouteResponse> getDeliveryRoutes(UUID deliveryId) {

        Delivery delivery = deliveryRepository
                .findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));

        return delivery.getRoutes()
                .stream()
                .filter(route -> route.getDeletedAt() == null)
                .map(this::toRouteResponse)
                .toList();
    }

    private DeliveryRouteResponse toRouteResponse(DeliveryRoute route) {

        return new DeliveryRouteResponse(
                route.getId(),
                route.getSequence(),
                route.getFromHubId(),
                route.getToHubId(),
                route.getEstimatedDistance(),
                route.getEstimatedDuration(),
                route.getActualDistance(),
                route.getActualDuration(),
                route.getDeliveryRouteStatus()
        );
    }
}
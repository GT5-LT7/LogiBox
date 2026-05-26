package com.sparta.gt5lt7.logisticsservice.application.service;

import com.sparta.gt5lt7.logisticsservice.application.event.OrderCreatedEvent;
import com.sparta.gt5lt7.logisticsservice.domain.entity.Delivery;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryRoute;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryRouteStatus;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryStatus;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryRepository;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryCreateService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteRepository deliveryRouteRepository;

    @Transactional
    public void createDeliveryFromOrder(OrderCreatedEvent event) {

        UUID fromHubId = event.supplierCompanyId();
        UUID toHubId = event.receiverCompanyId();

        Delivery delivery = Delivery.builder()
                .orderId(event.orderId())
                .fromHubId(fromHubId)
                .toHubId(toHubId)
                .receiverCompanyId(event.receiverCompanyId())
                .receiverAddress("배송지 주소 임시값")
                .deliveryAgentId(null)
                .deliveryStatus(DeliveryStatus.READY)
                .requestMessage(event.requestMessage())
                .aiLogId(UUID.randomUUID())
                .slackId(UUID.randomUUID())
                .createdBy(UUID.randomUUID())
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        DeliveryRoute route = DeliveryRoute.builder()
                .delivery(savedDelivery)
                .sequence(1)
                .fromHubId(fromHubId)
                .toHubId(toHubId)
                .estimatedDistance(0.0)
                .estimatedDuration(0)
                .deliveryRouteStatus(DeliveryRouteStatus.READY)
                .hubDeliveryAgentId(UUID.randomUUID())
                .createdBy(UUID.randomUUID())
                .build();

        deliveryRouteRepository.save(route);
    }
}

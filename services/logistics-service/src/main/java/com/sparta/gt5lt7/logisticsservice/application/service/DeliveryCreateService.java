package com.sparta.gt5lt7.logisticsservice.application.service;

import com.sparta.gt5lt7.logisticsservice.application.event.OrderCreatedEvent;
import com.sparta.gt5lt7.logisticsservice.domain.entity.Delivery;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryRoute;
import com.sparta.gt5lt7.logisticsservice.domain.entity.HubRoute;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryRouteStatus;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryStatus;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryRepository;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryCreateService {

    private final DeliveryRepository deliveryRepository;
    private final HubRouteRepository hubRouteRepository;

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

        List<HubRoute> hubRoutes = hubRouteRepository
                .findAllByFromHubIdAndDeletedAtIsNull(fromHubId);

        if (hubRoutes.isEmpty()) {
            throw new IllegalArgumentException("허브 간 배송 경로를 찾을 수 없습니다.");
        }

        for (int i = 0; i < hubRoutes.size(); i++) {
            HubRoute hubRoute = hubRoutes.get(i);

            DeliveryRoute deliveryRoute = DeliveryRoute.builder()
                    .delivery(delivery)
                    .sequence(i + 1)
                    .fromHubId(hubRoute.getFromHubId())
                    .toHubId(hubRoute.getToHubId())
                    .estimatedDistance(hubRoute.getDistance().doubleValue())
                    .estimatedDuration(hubRoute.getDuration())
                    .deliveryRouteStatus(DeliveryRouteStatus.READY)
                    .hubDeliveryAgentId(UUID.randomUUID())
                    .createdBy(UUID.randomUUID())
                    .build();

            delivery.addRoute(deliveryRoute);
        }

        deliveryRepository.save(delivery);
    }
}

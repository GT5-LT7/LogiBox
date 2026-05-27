package com.sparta.gt5lt7.logisticsservice.application.service;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Delivery;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryRoute;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryRepository;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryResponse;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryRouteResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryQueryServiceTest {

    @InjectMocks
    private DeliveryQueryService deliveryQueryService;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Test
    @DisplayName("배송 단건 조회 성공")
    void getDeliverySuccess() {
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = createDelivery(deliveryId);

        given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                .willReturn(Optional.of(delivery));

        DeliveryResponse response = deliveryQueryService.getDelivery(deliveryId);

        assertThat(response.deliveryId()).isEqualTo(deliveryId);
        assertThat(response.orderId()).isEqualTo(delivery.getOrderId());
        assertThat(response.fromHubId()).isEqualTo(delivery.getFromHubId());
        assertThat(response.toHubId()).isEqualTo(delivery.getToHubId());

        verify(deliveryRepository).findByIdAndDeletedAtIsNull(deliveryId);
    }

    @Test
    @DisplayName("배송 경로 조회 성공")
    void getDeliveryRoutesSuccess() {
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = createDelivery(deliveryId);

        DeliveryRoute route1 = createRoute(delivery, 1);
        DeliveryRoute route2 = createRoute(delivery, 2);

        delivery.addRoute(route1);
        delivery.addRoute(route2);

        given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                .willReturn(Optional.of(delivery));

        List<DeliveryRouteResponse> response =
                deliveryQueryService.getDeliveryRoutes(deliveryId);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).sequence()).isEqualTo(1);
        assertThat(response.get(1).sequence()).isEqualTo(2);

        verify(deliveryRepository).findByIdAndDeletedAtIsNull(deliveryId);
    }

    private Delivery createDelivery(UUID deliveryId) {
        Delivery delivery = Delivery.builder()
                .orderId(UUID.randomUUID())
                .fromHubId(UUID.randomUUID())
                .toHubId(UUID.randomUUID())
                .receiverCompanyId(UUID.randomUUID())
                .receiverAddress("서울시 강남구")
                .requestMessage("요청사항")
                .build();

        ReflectionTestUtils.setField(delivery, "id", deliveryId);
        ReflectionTestUtils.setField(delivery, "createdAt", LocalDateTime.now());
        return delivery;
    }

    private DeliveryRoute createRoute(Delivery delivery, int sequence) {
        DeliveryRoute route = DeliveryRoute.builder()
                .delivery(delivery)
                .sequence(sequence)
                .fromHubId(UUID.randomUUID())
                .toHubId(UUID.randomUUID())
                .estimatedDistance(10.0)
                .estimatedDuration(30)
                .hubDeliveryAgentId(UUID.randomUUID())
                .build();

        ReflectionTestUtils.setField(route, "id", UUID.randomUUID());
        return route;
    }
}
package com.sparta.gt5lt7.logisticsservice.application.service;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.logisticsservice.application.dto.DeliveryAccessContext;
import com.sparta.gt5lt7.logisticsservice.domain.entity.Delivery;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryRoute;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryDeleteServiceTest {

    @InjectMocks
    private DeliveryDeleteService deliveryDeleteService;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryPermissionValidator deliveryPermissionValidator;

    @Test
    @DisplayName("배송 삭제 시 배송 경로도 함께 논리 삭제")
    void softDeleteDeliveryAndRoutes() {

        // given
        UUID deliveryId = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();

        Delivery delivery = createDelivery(deliveryId);

        DeliveryRoute route = createRoute(delivery);

        delivery.addRoute(route);

        DeliveryAccessContext context =
                new DeliveryAccessContext(
                        deletedBy,
                        UserRole.ROLE_MASTER,
                        null,
                        null
                );

        given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                .willReturn(Optional.of(delivery));

        // when
        deliveryDeleteService.deleteDelivery(deliveryId, context);

        // then
        assertThat(delivery.getDeletedAt()).isNotNull();
        assertThat(route.getDeletedAt()).isNotNull();

        verify(deliveryRepository)
                .findByIdAndDeletedAtIsNull(deliveryId);

        verify(deliveryPermissionValidator)
                .validateDeletePermission(delivery, context);
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

        return delivery;
    }

    private DeliveryRoute createRoute(Delivery delivery) {

        return DeliveryRoute.builder()
                .delivery(delivery)
                .sequence(1)
                .fromHubId(UUID.randomUUID())
                .toHubId(UUID.randomUUID())
                .estimatedDistance(10.0)
                .estimatedDuration(30)
                .hubDeliveryAgentId(UUID.randomUUID())
                .build();
    }
}

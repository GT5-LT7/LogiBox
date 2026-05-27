package com.sparta.gt5lt7.logisticsservice.application.service;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Delivery;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryStatus;
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
class DeliveryUpdateServiceTest {

    @InjectMocks
    private DeliveryUpdateService deliveryUpdateService;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Test
    @DisplayName("배송 상태 수정 성공")
    void updateDeliveryStatusSuccess() {

        // given
        UUID deliveryId = UUID.randomUUID();

        Delivery delivery = createDelivery(deliveryId);

        given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                .willReturn(Optional.of(delivery));

        // when
        deliveryUpdateService.updateStatus(
                deliveryId,
                DeliveryStatus.IN_TRANSIT
        );

        // then
        assertThat(delivery.getDeliveryStatus())
                .isEqualTo(DeliveryStatus.IN_TRANSIT);

        verify(deliveryRepository)
                .findByIdAndDeletedAtIsNull(deliveryId);
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
}
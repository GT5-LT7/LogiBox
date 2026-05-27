package com.sparta.gt5lt7.logisticsservice.application.service;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.logisticsservice.application.dto.DeliveryAccessContext;
import com.sparta.gt5lt7.logisticsservice.domain.entity.Delivery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeliveryPermissionValidatorTest {

    private final DeliveryPermissionValidator validator =
            new DeliveryPermissionValidator();

    private final UUID userId = UUID.randomUUID();
    private final UUID fromHubId = UUID.randomUUID();
    private final UUID toHubId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();

    @Test
    @DisplayName("성공: MASTER는 배송 삭제 가능")
    void masterCanDeleteDelivery() {

        // given
        Delivery delivery = createDelivery(null);

        DeliveryAccessContext context =
                new DeliveryAccessContext(
                        userId,
                        UserRole.ROLE_MASTER,
                        null,
                        null
                );

        // when & then
        assertThatCode(() ->
                validator.validateDeletePermission(delivery, context)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: DELIVERY_MANAGER는 배송 삭제 불가")
    void deliveryManagerCannotDeleteDelivery() {

        // given
        Delivery delivery = createDelivery(userId);

        DeliveryAccessContext context =
                new DeliveryAccessContext(
                        userId,
                        UserRole.ROLE_DELIVERY_MANAGER,
                        null,
                        null
                );

        // when & then
        assertThatThrownBy(() ->
                validator.validateDeletePermission(delivery, context)
        ).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("성공: DELIVERY_MANAGER는 본인 배송 수정 가능")
    void deliveryManagerCanUpdateOwnDelivery() {

        // given
        Delivery delivery = createDelivery(userId);

        DeliveryAccessContext context =
                new DeliveryAccessContext(
                        userId,
                        UserRole.ROLE_DELIVERY_MANAGER,
                        null,
                        null
                );

        // when & then
        assertThatCode(() ->
                validator.validateUpdatePermission(delivery, context)
        ).doesNotThrowAnyException();
    }

    private Delivery createDelivery(UUID deliveryAgentId) {

        return Delivery.builder()
                .orderId(UUID.randomUUID())
                .fromHubId(fromHubId)
                .toHubId(toHubId)
                .receiverCompanyId(companyId)
                .receiverAddress("서울시 강남구")
                .deliveryAgentId(deliveryAgentId)
                .requestMessage("문 앞에 두세요")
                .build();
    }
}
package com.sparta.gt5lt7.logisticsservice.application.service;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.logisticsservice.application.dto.DeliveryAccessContext;
import com.sparta.gt5lt7.logisticsservice.domain.entity.Delivery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeliveryPermissionValidator {

    public void validateDeletePermission(
            Delivery delivery,
            DeliveryAccessContext context
    ) {
        UserRole role = context.role();

        if (role == UserRole.ROLE_MASTER) {
            return;
        }

        if (role == UserRole.ROLE_HUB_MANAGER && isManagedHubDelivery(delivery, context)) {
            return;
        }

        throw new AccessDeniedException("배송 삭제 권한이 없습니다.");
    }

    public void validateUpdatePermission(
            Delivery delivery,
            DeliveryAccessContext context
    ) {
        UserRole role = context.role();

        if (role == UserRole.ROLE_MASTER) {
            return;
        }

        if (role == UserRole.ROLE_HUB_MANAGER && isManagedHubDelivery(delivery, context)) {
            return;
        }

        if (role == UserRole.ROLE_DELIVERY_MANAGER && isAssignedDelivery(delivery, context)) {
            return;
        }

        throw new AccessDeniedException("배송 수정 권한이 없습니다.");
    }

    public void validateReadPermission(
            Delivery delivery,
            DeliveryAccessContext context
    ) {
        UserRole role = context.role();

        if (role == UserRole.ROLE_MASTER) {
            return;
        }

        if (role == UserRole.ROLE_HUB_MANAGER && isManagedHubDelivery(delivery, context)) {
            return;
        }

        if (role == UserRole.ROLE_DELIVERY_MANAGER && isAssignedDelivery(delivery, context)) {
            return;
        }

        if (role == UserRole.ROLE_COMPANY_MANAGER && isCompanyDelivery(delivery, context)) {
            return;
        }

        throw new AccessDeniedException("배송 조회 권한이 없습니다.");
    }

    private boolean isManagedHubDelivery(
            Delivery delivery,
            DeliveryAccessContext context
    ) {
        if (context.hubId() == null) {
            return false;
        }

        return context.hubId().equals(delivery.getFromHubId())
                || context.hubId().equals(delivery.getToHubId());
    }

    private boolean isAssignedDelivery(
            Delivery delivery,
            DeliveryAccessContext context
    ) {
        if (delivery.getDeliveryAgentId() == null) {
            return false;
        }

        return delivery.getDeliveryAgentId().equals(context.userId());
    }

    private boolean isCompanyDelivery(
            Delivery delivery,
            DeliveryAccessContext context
    ) {
        if (context.companyId() == null) {
            return false;
        }

        return context.companyId().equals(delivery.getReceiverCompanyId());
    }
}

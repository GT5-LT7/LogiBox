package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.domain.entity.HubRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HubRouteRepository
        extends JpaRepository<HubRoute, UUID>, HubRouteRepositoryCustom {

    boolean existsByFromHubIdAndToHubIdAndDeletedAtIsNull(UUID fromHubId, UUID toHubId);

    Optional<HubRoute> findByRouteIdAndDeletedAtIsNull(UUID routeId);

    Optional<HubRoute> findByFromHubIdAndToHubIdAndDeletedAtIsNull(UUID fromHubId, UUID toHubId);
}
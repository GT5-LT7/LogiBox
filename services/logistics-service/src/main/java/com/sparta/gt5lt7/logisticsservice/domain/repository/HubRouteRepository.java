package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.domain.entity.HubRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubRouteRepository
        extends JpaRepository<HubRoute, UUID>, HubRouteRepositoryCustom {

    boolean existsByFromHubIdAndToHubIdAndDeletedAtIsNull(UUID fromHubId, UUID toHubId);

    Optional<HubRoute> findByRouteIdAndDeletedAtIsNull(UUID routeId);

    Optional<HubRoute> findByFromHubIdAndToHubIdAndDeletedAtIsNull(UUID fromHubId, UUID toHubId);

    // 특정 허브 출발 전체 경로 조회 (SA 기반 구현)
    List<HubRoute> findAllByFromHubIdAndDeletedAtIsNull(UUID fromHubId);
}
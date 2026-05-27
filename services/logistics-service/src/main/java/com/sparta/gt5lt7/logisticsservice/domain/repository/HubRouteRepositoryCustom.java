package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.domain.entity.HubRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HubRouteRepositoryCustom {

    Page<HubRoute> searchHubRoutes(UUID fromHubId, UUID toHubId, Pageable pageable);
}
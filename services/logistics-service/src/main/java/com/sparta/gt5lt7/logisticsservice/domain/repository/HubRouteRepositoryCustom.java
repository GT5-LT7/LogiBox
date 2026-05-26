package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRouteSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubRouteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubRouteRepositoryCustom {

    Page<HubRouteResponse> searchHubRoutes(HubRouteSearchRequest request, Pageable pageable);

}

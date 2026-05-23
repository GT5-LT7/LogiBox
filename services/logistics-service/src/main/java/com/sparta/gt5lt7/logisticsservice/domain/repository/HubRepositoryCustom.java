package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubRepositoryCustom {

    Page<HubResponse> searchHubs(HubSearchRequest request, Pageable pageable);
}
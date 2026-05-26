package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryAgentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliveryAgentRepositoryCustom {
    Page<DeliveryAgentResponse> searchDeliveryAgents(
            DeliveryAgentSearchRequest request, Pageable pageable
    );
}
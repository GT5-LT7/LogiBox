package com.sparta.gt5lt7.logisticsservice.application;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRepository;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubException;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubService {

    private final HubRepository hubRepository;

    @Transactional
    public HubResponse createHub(HubRequest request) {

        if (hubRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new HubException(HubErrorCode.HUB_NAME_DUPLICATED);
        }

        try {
            Hub hub = Hub.create(
                    request.getName(),
                    request.getAddress(),
                    request.getLatitude(),
                    request.getLongitude()
            );

            return HubResponse.from(hubRepository.save(hub));

        } catch (DataIntegrityViolationException e) {
            throw new HubException(HubErrorCode.HUB_NAME_DUPLICATED);
        }
    }
}
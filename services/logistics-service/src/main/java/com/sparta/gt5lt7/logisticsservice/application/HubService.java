package com.sparta.gt5lt7.logisticsservice.application;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRepository;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubException;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubUpdateRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    // 허브 상세 조회 (캐싱 적용)
    @Cacheable(cacheNames = "hub", key = "#hubId")
    public HubResponse getHub(UUID hubId) {
        Hub hub = hubRepository.findByHubIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new HubException(HubErrorCode.HUB_NOT_FOUND));
        return HubResponse.from(hub);
    }

    // 허브 목록 검색
    public Page<HubResponse> searchHubs(HubSearchRequest request, Pageable pageable) {
        return hubRepository.searchHubs(request, pageable);
    }

    @Transactional
    @CacheEvict(cacheNames = "hub", key = "#hubId")
    public HubResponse updateHub(UUID hubId, HubUpdateRequest request) {
        Hub hub = hubRepository.findByHubIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new HubException(HubErrorCode.HUB_NOT_FOUND));

        if (request.getName() != null
                && !request.getName().equals(hub.getName())
                && hubRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new HubException(HubErrorCode.HUB_NAME_DUPLICATED);
        }

        try {
            hub.update(
                    request.getName(),
                    request.getAddress(),
                    request.getLatitude(),
                    request.getLongitude()
            );
            return HubResponse.from(hub);
        } catch (DataIntegrityViolationException e) {
            throw new HubException(HubErrorCode.HUB_NAME_DUPLICATED);
        }
    }
}
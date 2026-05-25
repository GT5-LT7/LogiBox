package com.sparta.gt5lt7.logisticsservice.application;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import com.sparta.gt5lt7.logisticsservice.domain.entity.HubRoute;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRepository;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRouteRepository;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubException;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubRouteErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubRouteException;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRouteRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubRouteService {

    private final HubRouteRepository hubRouteRepository;
    private final HubRepository hubRepository;

    @Transactional
    @CacheEvict(cacheNames = "hubRoutes", allEntries = true)
    public HubRouteResponse createHubRoute(HubRouteRequest request) {
        // 1. 출발/도착 허브가 동일한지 검증 (DTO @AssertTrue 보강)
        if (request.getFromHubId().equals(request.getToHubId())) {
            throw new HubRouteException(HubRouteErrorCode.HUB_ROUTE_SAME_HUB);
        }

        // 2. 두 허브가 실제로 존재하고 활성 상태인지 검증
        Hub fromHub = hubRepository.findByHubIdAndDeletedAtIsNull(request.getFromHubId())
                .orElseThrow(() -> new HubException(HubErrorCode.HUB_NOT_FOUND));
        Hub toHub = hubRepository.findByHubIdAndDeletedAtIsNull(request.getToHubId())
                .orElseThrow(() -> new HubException(HubErrorCode.HUB_NOT_FOUND));

        // 3. 중복 경로 확인 (활성 상태 기준)
        if (hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(
                fromHub.getHubId(), toHub.getHubId())) {
            throw new HubRouteException(HubRouteErrorCode.HUB_ROUTE_DUPLICATED);
        }

        try {
            HubRoute route = HubRoute.create(
                    fromHub.getHubId(),
                    toHub.getHubId(),
                    request.getDistance(),
                    request.getDuration()
            );
            return HubRouteResponse.from(hubRouteRepository.save(route));
        } catch (DataIntegrityViolationException e) {
            throw new HubRouteException(HubRouteErrorCode.HUB_ROUTE_DUPLICATED);
        }
    }
}
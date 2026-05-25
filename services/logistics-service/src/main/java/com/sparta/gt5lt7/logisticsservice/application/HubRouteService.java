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

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubRouteService {

    // PostgreSQL & H2 unique_violation SQLState
    private static final String SQLSTATE_UNIQUE_VIOLATION = "23505";

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
            if (isUniqueConstraintViolation(e)) {
                throw new HubRouteException(HubRouteErrorCode.HUB_ROUTE_DUPLICATED);
            }
            throw e;
        }
    }

    // DataIntegrityViolationException의 원인 체인을 따라가서 SQLState가 unique_violation(23505)인지 확인. PostgreSQL, H2 공통으로 23505를 사용.

    private boolean isUniqueConstraintViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof SQLException sqlEx) {
                return SQLSTATE_UNIQUE_VIOLATION.equals(sqlEx.getSQLState());
            }
            cause = cause.getCause();
        }
        return false;
    }
}
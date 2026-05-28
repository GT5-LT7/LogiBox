package com.sparta.gt5lt7.logisticsservice.application;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import com.sparta.gt5lt7.logisticsservice.domain.entity.HubRoute;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRepository;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRouteRepository;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubException;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubRouteErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubRouteException;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRouteRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRouteSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubRouteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HubRouteServiceTest {

    @Mock
    private HubRouteRepository hubRouteRepository;

    @Mock
    private HubRepository hubRepository;

    @InjectMocks
    private HubRouteService hubRouteService;

    private Hub fromHub;
    private Hub toHub;
    private HubRoute hubRoute;
    private UUID fromHubId;
    private UUID toHubId;
    private UUID routeId;

    @BeforeEach
    void setUp() {
        fromHubId = UUID.randomUUID();
        toHubId = UUID.randomUUID();
        routeId = UUID.randomUUID();

        fromHub = Hub.builder()
                .hubId(fromHubId)
                .name("서울허브")
                .address("서울시")
                .latitude(37.5)
                .longitude(127.0)
                .build();

        toHub = Hub.builder()
                .hubId(toHubId)
                .name("부산허브")
                .address("부산시")
                .latitude(35.1)
                .longitude(129.0)
                .build();

        hubRoute = HubRoute.builder()
                .routeId(routeId)
                .fromHubId(fromHubId)
                .toHubId(toHubId)
                .distance(400)
                .duration(240)
                .build();
    }


    @Test
    @DisplayName("허브 경로 생성 - 정상 케이스: 저장된 경로 정보를 반환한다")
    void createHubRoute() {
        // given
        HubRouteRequest request = mock(HubRouteRequest.class);
        given(request.getFromHubId()).willReturn(fromHubId);
        given(request.getToHubId()).willReturn(toHubId);
        given(request.getDistance()).willReturn(400);
        given(request.getDuration()).willReturn(240);

        given(hubRepository.findByHubIdAndDeletedAtIsNull(fromHubId)).willReturn(Optional.of(fromHub));
        given(hubRepository.findByHubIdAndDeletedAtIsNull(toHubId)).willReturn(Optional.of(toHub));
        given(hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId))
                .willReturn(false);
        given(hubRouteRepository.save(any(HubRoute.class))).willReturn(hubRoute);

        // when
        HubRouteResponse response = hubRouteService.createHubRoute(request);

        // then
        assertThat(response.getFromHubId()).isEqualTo(fromHubId);
        assertThat(response.getToHubId()).isEqualTo(toHubId);
        assertThat(response.getDistance()).isEqualTo(400);
        assertThat(response.getDuration()).isEqualTo(240);
        verify(hubRouteRepository, times(1)).save(any(HubRoute.class));
    }


    @Test
    @DisplayName("허브 경로 생성 - 출발과 도착 허브가 동일하면 HUB_ROUTE_SAME_HUB 예외를 던진다")
    void createHubRoute_sameHub() {
        // given
        HubRouteRequest request = mock(HubRouteRequest.class);
        given(request.getFromHubId()).willReturn(fromHubId);
        given(request.getToHubId()).willReturn(fromHubId);  // 동일한 허브

        // when & then
        assertThatThrownBy(() -> hubRouteService.createHubRoute(request))
                .isInstanceOf(HubRouteException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubRouteErrorCode.HUB_ROUTE_SAME_HUB);

        verify(hubRouteRepository, never()).save(any(HubRoute.class));
    }


    @Test
    @DisplayName("허브 경로 생성 - 출발 허브가 존재하지 않으면 HUB_NOT_FOUND 예외를 던진다")
    void createHubRoute_fromHubNotFound() {
        // given
        HubRouteRequest request = mock(HubRouteRequest.class);
        given(request.getFromHubId()).willReturn(fromHubId);
        given(request.getToHubId()).willReturn(toHubId);
        given(hubRepository.findByHubIdAndDeletedAtIsNull(fromHubId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hubRouteService.createHubRoute(request))
                .isInstanceOf(HubException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubErrorCode.HUB_NOT_FOUND);

        verify(hubRouteRepository, never()).save(any(HubRoute.class));
    }


    @Test
    @DisplayName("허브 경로 생성 - 이미 존재하는 경로 페어면 HUB_ROUTE_DUPLICATED 예외를 던진다 (멱등성)")
    void createHubRoute_duplicatedPair() {
        // given
        HubRouteRequest request = mock(HubRouteRequest.class);
        given(request.getFromHubId()).willReturn(fromHubId);
        given(request.getToHubId()).willReturn(toHubId);

        given(hubRepository.findByHubIdAndDeletedAtIsNull(fromHubId)).willReturn(Optional.of(fromHub));
        given(hubRepository.findByHubIdAndDeletedAtIsNull(toHubId)).willReturn(Optional.of(toHub));
        given(hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> hubRouteService.createHubRoute(request))
                .isInstanceOf(HubRouteException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubRouteErrorCode.HUB_ROUTE_DUPLICATED);

        verify(hubRouteRepository, never()).save(any(HubRoute.class));
    }


    @Test
    @DisplayName("허브 경로 생성 - 동시 요청 시 DB 유니크 제약 위반(SQLState 23505)을 HUB_ROUTE_DUPLICATED로 변환한다")
    void createHubRoute_uniqueConstraintViolation() {
        // given
        HubRouteRequest request = mock(HubRouteRequest.class);
        given(request.getFromHubId()).willReturn(fromHubId);
        given(request.getToHubId()).willReturn(toHubId);
        given(request.getDistance()).willReturn(400);
        given(request.getDuration()).willReturn(240);

        given(hubRepository.findByHubIdAndDeletedAtIsNull(fromHubId)).willReturn(Optional.of(fromHub));
        given(hubRepository.findByHubIdAndDeletedAtIsNull(toHubId)).willReturn(Optional.of(toHub));
        given(hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId))
                .willReturn(false);

        // 동시 요청 시 다른 트랜잭션이 먼저 저장해 DB unique violation 발생 케이스
        SQLException sqlException = new SQLException("duplicate key", "23505");
        DataIntegrityViolationException dive = new DataIntegrityViolationException("unique violation", sqlException);
        given(hubRouteRepository.save(any(HubRoute.class))).willThrow(dive);

        // when & then
        assertThatThrownBy(() -> hubRouteService.createHubRoute(request))
                .isInstanceOf(HubRouteException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubRouteErrorCode.HUB_ROUTE_DUPLICATED);
    }


    @Test
    @DisplayName("허브 경로 단건 조회 - 정상 케이스: 경로 정보를 반환한다")
    void getHubRoute() {
        // given
        given(hubRouteRepository.findByRouteIdAndDeletedAtIsNull(routeId)).willReturn(Optional.of(hubRoute));

        // when
        HubRouteResponse response = hubRouteService.getHubRoute(routeId);

        // then
        assertThat(response.getRouteId()).isEqualTo(routeId);
        assertThat(response.getFromHubId()).isEqualTo(fromHubId);
        assertThat(response.getToHubId()).isEqualTo(toHubId);
    }


    @Test
    @DisplayName("허브 경로 단건 조회 - 존재하지 않으면 HUB_ROUTE_NOT_FOUND 예외를 던진다")
    void getHubRoute_notFound() {
        // given
        given(hubRouteRepository.findByRouteIdAndDeletedAtIsNull(routeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hubRouteService.getHubRoute(routeId))
                .isInstanceOf(HubRouteException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubRouteErrorCode.HUB_ROUTE_NOT_FOUND);
    }


    @Test
    @DisplayName("허브 경로 페어 조회 - 정상 케이스: 출발-도착 페어의 경로를 반환한다")
    void getHubRouteByHubs() {
        // given
        given(hubRouteRepository.findByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId))
                .willReturn(Optional.of(hubRoute));

        // when
        HubRouteResponse response = hubRouteService.getHubRouteByHubs(fromHubId, toHubId);

        // then
        assertThat(response.getFromHubId()).isEqualTo(fromHubId);
        assertThat(response.getToHubId()).isEqualTo(toHubId);
    }


    @Test
    @DisplayName("허브 경로 페어 조회 - 출발과 도착이 동일하면 HUB_ROUTE_SAME_HUB 예외를 던진다")
    void getHubRouteByHubs_sameHub() {
        // when & then
        assertThatThrownBy(() -> hubRouteService.getHubRouteByHubs(fromHubId, fromHubId))
                .isInstanceOf(HubRouteException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubRouteErrorCode.HUB_ROUTE_SAME_HUB);
    }


    @Test
    @DisplayName("허브 경로 페어 조회 - 페어가 존재하지 않으면 HUB_ROUTE_NOT_FOUND 예외를 던진다")
    void getHubRouteByHubs_notFound() {
        // given
        given(hubRouteRepository.findByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hubRouteService.getHubRouteByHubs(fromHubId, toHubId))
                .isInstanceOf(HubRouteException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubRouteErrorCode.HUB_ROUTE_NOT_FOUND);
    }


    @Test
    @DisplayName("허브 경로 검색 - 출발/도착 조건 기반 페이징 결과를 반환한다")
    void searchHubRoutes() {
        // given
        HubRouteSearchRequest request = new HubRouteSearchRequest(fromHubId, toHubId);
        Pageable pageable = PageRequest.of(0, 10);
        Page<HubRoute> routePage = new PageImpl<>(List.of(hubRoute), pageable, 1);

        given(hubRouteRepository.searchHubRoutes(fromHubId, toHubId, pageable)).willReturn(routePage);

        // when
        Page<HubRouteResponse> result = hubRouteService.searchHubRoutes(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRouteId()).isEqualTo(routeId);
    }


    @Test
    @DisplayName("허브 경로 삭제 - 정상 케이스: 소프트 삭제 처리되고 deletedAt/deletedBy가 기록된다")
    void deleteHubRoute() {
        // given
        UUID userId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, UserRole.ROLE_MASTER, null, null);

        given(hubRouteRepository.findByRouteIdAndDeletedAtIsNull(routeId)).willReturn(Optional.of(hubRoute));

        // when
        HubRouteResponse response = hubRouteService.deleteHubRoute(routeId, principal);

        // then
        assertThat(response.getRouteId()).isEqualTo(routeId);
        assertThat(hubRoute.getDeletedAt()).isNotNull();
        assertThat(hubRoute.getDeletedBy()).isEqualTo(userId);
    }


    @Test
    @DisplayName("허브 경로 삭제 - 존재하지 않으면 HUB_ROUTE_NOT_FOUND 예외를 던진다")
    void deleteHubRoute_notFound() {
        // given
        UUID userId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, UserRole.ROLE_MASTER, null, null);

        given(hubRouteRepository.findByRouteIdAndDeletedAtIsNull(routeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hubRouteService.deleteHubRoute(routeId, principal))
                .isInstanceOf(HubRouteException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubRouteErrorCode.HUB_ROUTE_NOT_FOUND);
    }
}
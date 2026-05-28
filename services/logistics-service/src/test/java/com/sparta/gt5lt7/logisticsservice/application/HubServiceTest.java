package com.sparta.gt5lt7.logisticsservice.application;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRepository;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubException;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubUpdateRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
class HubServiceTest {

    @Mock
    private HubRepository hubRepository;

    @InjectMocks
    private HubService hubService;

    private Hub hub;
    private UUID hubId;

    @BeforeEach
    void setUp() {
        hubId = UUID.randomUUID();
        hub = Hub.builder()
                .hubId(hubId)
                .name("서울허브")
                .address("서울시 송파구")
                .latitude(37.5)
                .longitude(127.1)
                .build();
    }


    @Test
    @DisplayName("허브 생성 - 정상 케이스: 저장된 허브 정보를 반환한다")
    void createHub() {
        // given
        HubRequest request = mock(HubRequest.class);
        given(request.getName()).willReturn("서울허브");
        given(request.getAddress()).willReturn("서울시 송파구");
        given(request.getLatitude()).willReturn(37.5);
        given(request.getLongitude()).willReturn(127.1);

        given(hubRepository.existsByNameAndDeletedAtIsNull("서울허브")).willReturn(false);
        given(hubRepository.save(any(Hub.class))).willReturn(hub);

        // when
        HubResponse response = hubService.createHub(request);

        // then
        assertThat(response.getName()).isEqualTo("서울허브");
        assertThat(response.getAddress()).isEqualTo("서울시 송파구");
        assertThat(response.getLatitude()).isEqualTo(37.5);
        assertThat(response.getLongitude()).isEqualTo(127.1);
        verify(hubRepository, times(1)).save(any(Hub.class));
    }


    @Test
    @DisplayName("허브 생성 - 중복 이름이면 HUB_NAME_DUPLICATED 예외를 던진다")
    void createHub_duplicatedName() {
        // given
        HubRequest request = mock(HubRequest.class);
        given(request.getName()).willReturn("서울허브");
        given(hubRepository.existsByNameAndDeletedAtIsNull("서울허브")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> hubService.createHub(request))
                .isInstanceOf(HubException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubErrorCode.HUB_NAME_DUPLICATED);

        verify(hubRepository, never()).save(any(Hub.class));
    }


    @Test
    @DisplayName("허브 조회 - 정상 케이스: 허브 정보를 반환한다")
    void getHub() {
        // given
        given(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).willReturn(Optional.of(hub));

        // when
        HubResponse response = hubService.getHub(hubId);

        // then
        assertThat(response.getHubId()).isEqualTo(hubId);
        assertThat(response.getName()).isEqualTo("서울허브");
    }


    @Test
    @DisplayName("허브 조회 - 존재하지 않는 허브면 HUB_NOT_FOUND 예외를 던진다")
    void getHub_notFound() {
        // given
        given(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hubService.getHub(hubId))
                .isInstanceOf(HubException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubErrorCode.HUB_NOT_FOUND);
    }


    @Test
    @DisplayName("허브 검색 - 키워드 기반 페이징 결과를 반환한다")
    void searchHubs() {
        // given
        HubSearchRequest request = new HubSearchRequest("서울");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hub> hubPage = new PageImpl<>(List.of(hub), pageable, 1);

        given(hubRepository.searchHubs("서울", pageable)).willReturn(hubPage);

        // when
        Page<HubResponse> result = hubService.searchHubs(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("서울허브");
    }


    @Test
    @DisplayName("허브 수정 - 정상 케이스: 변경된 필드가 반영된 응답을 반환한다")
    void updateHub() {
        // given
        HubUpdateRequest request = mock(HubUpdateRequest.class);
        given(request.getName()).willReturn("서울허브 변경");
        given(request.getAddress()).willReturn("서울시 강남구");
        given(request.getLatitude()).willReturn(37.6);
        given(request.getLongitude()).willReturn(127.2);

        given(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).willReturn(Optional.of(hub));
        given(hubRepository.existsByNameAndDeletedAtIsNull("서울허브 변경")).willReturn(false);
        given(hubRepository.saveAndFlush(hub)).willReturn(hub);

        // when
        HubResponse response = hubService.updateHub(hubId, request);

        // then
        assertThat(response.getName()).isEqualTo("서울허브 변경");
        assertThat(response.getAddress()).isEqualTo("서울시 강남구");
        assertThat(response.getLatitude()).isEqualTo(37.6);
        assertThat(response.getLongitude()).isEqualTo(127.2);
    }


    @Test
    @DisplayName("허브 수정 - 존재하지 않는 허브면 HUB_NOT_FOUND 예외를 던진다")
    void updateHub_notFound() {
        // given
        HubUpdateRequest request = mock(HubUpdateRequest.class);
        given(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hubService.updateHub(hubId, request))
                .isInstanceOf(HubException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubErrorCode.HUB_NOT_FOUND);
    }


    @Test
    @DisplayName("허브 수정 - 변경하려는 이름이 다른 허브와 중복되면 HUB_NAME_DUPLICATED 예외를 던진다")
    void updateHub_duplicatedName() {
        // given
        HubUpdateRequest request = mock(HubUpdateRequest.class);
        given(request.getName()).willReturn("부산허브");  // 기존 이름과 다른 이름
        given(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).willReturn(Optional.of(hub));
        given(hubRepository.existsByNameAndDeletedAtIsNull("부산허브")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> hubService.updateHub(hubId, request))
                .isInstanceOf(HubException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubErrorCode.HUB_NAME_DUPLICATED);

        verify(hubRepository, never()).saveAndFlush(any(Hub.class));
    }


    @Test
    @DisplayName("허브 삭제 - 정상 케이스: 소프트 삭제 처리되고 deletedAt/deletedBy가 기록된다")
    void deleteHub() {
        // given
        UUID userId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, UserRole.ROLE_MASTER, null, null);

        given(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).willReturn(Optional.of(hub));

        // when
        HubResponse response = hubService.deleteHub(hubId, principal);

        // then
        assertThat(response.getHubId()).isEqualTo(hubId);
        assertThat(hub.getDeletedAt()).isNotNull();
        assertThat(hub.getDeletedBy()).isEqualTo(userId);
    }


    @Test
    @DisplayName("허브 삭제 - 존재하지 않는 허브면 HUB_NOT_FOUND 예외를 던진다")
    void deleteHub_notFound() {
        // given
        UUID userId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, UserRole.ROLE_MASTER, null, null);

        given(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hubService.deleteHub(hubId, principal))
                .isInstanceOf(HubException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubErrorCode.HUB_NOT_FOUND);
    }
}
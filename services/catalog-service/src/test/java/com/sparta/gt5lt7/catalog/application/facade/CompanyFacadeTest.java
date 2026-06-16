package com.sparta.gt5lt7.catalog.application.facade;

import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.common.dto.PageResponse;
import com.sparta.gt5lt7.catalog.application.service.CompanyService;
import com.sparta.gt5lt7.catalog.application.service.KakaoMapService;
import com.sparta.gt5lt7.catalog.application.service.ProductService;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.global.exception.CompanyErrorCode;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.UserClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.request.CompanyRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CoordinateResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.response.HubUsageStatusResponse;
import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyFacadeTest {
    @InjectMocks
    private CompanyFacade companyFacade;

    @Mock
    private CompanyService companyService;

    @Mock
    private ProductService productService;

    @Mock
    private KakaoMapService kakaoMapService;

    @Mock
    private HubClient hubClient;

    @Mock
    private UserClient userClient;

    private final UUID companyId = UUID.randomUUID();
    private final UUID hubId = UUID.randomUUID();

    private final HubResponse mockHub  = new HubResponse(hubId, "서울 중앙 허브");

    @Nested
    @DisplayName("업체 생성 테스트")
    class CreateCompanyTest {
        private final CompanyRequest companyRequest = createCompanyRequest("스파르타 물류");
        private final Company mockCompany = createCompany(companyId, companyRequest);
        private final CoordinateResponse mockCoordinate = new CoordinateResponse(
                new BigDecimal("37.5"), new BigDecimal("127.0")
        );
        ApiResponse<HubResponse> mockHubFeign = ApiResponse.success(mockHub);

        @Test
        @DisplayName("성공: MASTER - 허브 상관 없음")
        void test1() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_MASTER, null);
            given(hubClient.getHub(hubId)).willReturn(mockHubFeign);
            given(kakaoMapService.getCoordinate(companyRequest.baseAddress())).willReturn(mockCoordinate);
            given(companyService.createCompany(companyRequest, mockCoordinate)).willReturn(mockCompany);

            // when
            CompanyResponse.Create response = companyFacade.createCompany(companyRequest, principal);

            // then
            assertThat(response.name()).isEqualTo(companyRequest.name());
            verify(hubClient).getHub(hubId);
            verify(kakaoMapService).getCoordinate(companyRequest.baseAddress());
            verify(companyService).createCompany(companyRequest, mockCoordinate);
        }

        @Test
        @DisplayName("성공: HUB_MANAGER - 담당 허브")
        void test2() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_HUB_MANAGER, hubId);
            given(hubClient.getHub(companyRequest.hubId())).willReturn(mockHubFeign);
            given(kakaoMapService.getCoordinate(companyRequest.baseAddress())).willReturn(mockCoordinate);
            given(companyService.createCompany(companyRequest, mockCoordinate)).willReturn(mockCompany);

            // when
            CompanyResponse.Create response = companyFacade.createCompany(companyRequest, principal);

            // then
            assertThat(response).isNotNull();
            verify(hubClient).getHub(companyRequest.hubId());
            verify(kakaoMapService).getCoordinate(companyRequest.baseAddress());
            verify(companyService).createCompany(companyRequest, mockCoordinate);
        }

        @Test
        @DisplayName("실패: HUB_MANAGER - 담당 허브 아님")
        void test3() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_HUB_MANAGER, UUID.randomUUID());

            // when & then
            assertThatThrownBy(() -> companyFacade.createCompany(companyRequest, principal))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_CREATE_DENIED.getMessage());

            verifyNoInteractions(hubClient);
            verifyNoInteractions(kakaoMapService);
            verifyNoInteractions(companyService);
        }
    }

    @Test
    @DisplayName("업체 목록 조회 테스트")
    void searchCompaniesTest() {
        // given
        String keyword = "스파르타";
        CompanyType type = CompanyType.SUPPLIER;
        Pageable pageable = PageRequest.of(0, 10);

        Company mockCompany = createCompany(UUID.randomUUID(), createCompanyRequest("스파르타 물류"));
        Page<Company> mockCompanyPage = new PageImpl<>(List.of(mockCompany), pageable, 1);

        HubResponse mockHub = new HubResponse(hubId, "서울 중앙 허브");
        ApiResponse<List<HubResponse>> mockHubFeign = ApiResponse.success(List.of(mockHub));

        Set<UUID> targetHubIds = Set.of(hubId);

        given(companyService.searchCompanies(keyword, type, hubId, pageable)).willReturn(mockCompanyPage);
        given(hubClient.getHubs(targetHubIds)).willReturn(mockHubFeign);

        // when
        PageResponse<CompanyResponse.Summary> response = companyFacade.searchCompanies(keyword, type, hubId, pageable);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).info().name()).isEqualTo(mockCompany.getName());
        verify(companyService).searchCompanies(keyword, type, hubId, pageable);
        verify(hubClient).getHubs(targetHubIds);
    }

    @Test
    @DisplayName("업체 조회 테스트")
    void getCompanyTest() {
        // given
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();

        Company mockCompany = createCompany(companyId, createCompanyRequest("스파르타 물류"));
        ReflectionTestUtils.setField(mockCompany, "createdBy", createdBy);
        ReflectionTestUtils.setField(mockCompany, "updatedBy", updatedBy);

        List<UserResponse> mockUsers = List.of(
                new UserResponse(createdBy, "생성자"), new UserResponse(updatedBy, "수정자")
        );

        ApiResponse<HubResponse> mockHubFeign = ApiResponse.success(mockHub);
        ApiResponse<List<UserResponse>> mockUserFeign = ApiResponse.success(mockUsers);

        given(companyService.getCompany(companyId)).willReturn(mockCompany);
        given(hubClient.getHub(hubId)).willReturn(mockHubFeign);
        given(userClient.getUsers(Set.of(createdBy, updatedBy))).willReturn(mockUserFeign);

        // when
        CompanyResponse.Detail response = companyFacade.getCompany(companyId);

        // then
        assertThat(response.info().name()).isEqualTo(mockCompany.getName());
        verify(companyService).getCompany(companyId);
        verify(hubClient).getHub(hubId);
        verify(userClient).getUsers(Set.of(createdBy, updatedBy));
    }

    @Test
    @DisplayName("허브 사용 여부 조회 테스트")
    void checkHubUsageTest() {
        // given
        given(companyService.checkHubUsage(hubId)).willReturn(true);

        // when
        HubUsageStatusResponse response = companyFacade.checkHubUsage(hubId);

        // then
        assertThat(response.used()).isTrue();
        verify(companyService).checkHubUsage(hubId);
    }

    @Nested
    @DisplayName("업체 수정 테스트")
    class UpdateCompanyTest {
        private final CompanyRequest request = createCompanyRequest("스파르타 물류");
        private final Company mockCompany = createCompany(companyId, request);
        private final ApiResponse<HubResponse> mockHubFeign = ApiResponse.success(mockHub);

        @Test
        @DisplayName("성공: MASTER - 허브와 업체 상관 없음")
        void test1() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_MASTER, null);
            given(companyService.getCompany(companyId)).willReturn(mockCompany);
            given(companyService.updateCompany(mockCompany, request, null)).willReturn(mockCompany);
            given(hubClient.getHub(hubId)).willReturn(mockHubFeign);

            // when
            CompanyResponse.Update response = companyFacade.updateCompany(companyId, request, principal);

            // then
            assertThat(response.info().name()).isEqualTo(request.name());
            verify(companyService).getCompany(companyId);
            verify(companyService).updateCompany(mockCompany, request, null);
            verify(hubClient).getHub(hubId);
            verifyNoInteractions(kakaoMapService);
        }

        @Test
        @DisplayName("성공: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 일치")
        void test2() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_HUB_MANAGER, hubId);
            given(companyService.getCompany(companyId)).willReturn(mockCompany);
            given(companyService.updateCompany(mockCompany, request, null)).willReturn(mockCompany);
            given(hubClient.getHub(hubId)).willReturn(mockHubFeign);

            // when
            CompanyResponse.Update response = companyFacade.updateCompany(companyId, request, principal);

            // then
            assertThat(response).isNotNull();
            verify(companyService).getCompany(companyId);
            verify(companyService).updateCompany(mockCompany, request, null);
            verify(hubClient).getHub(hubId);
            verifyNoInteractions(kakaoMapService);
        }

        @Test
        @DisplayName("성공: COMPANY_MANAGER - 본인 업체 ID와 요청 업체 ID 일치")
        void test3() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_COMPANY_MANAGER, companyId);
            given(companyService.getCompany(companyId)).willReturn(mockCompany);
            given(companyService.updateCompany(mockCompany, request, null)).willReturn(mockCompany);
            given(hubClient.getHub(hubId)).willReturn(mockHubFeign);

            // when
            CompanyResponse.Update response = companyFacade.updateCompany(companyId, request, principal);

            // then
            assertThat(response).isNotNull();
            verify(companyService).getCompany(companyId);
            verify(companyService).updateCompany(mockCompany, request, null);
            verify(hubClient).getHub(hubId);
            verifyNoInteractions(kakaoMapService);
        }

        @Test
        @DisplayName("성공: 주소 변경 시 카카오맵 API를 호출해 위·경도 갱신")
        void test4() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_MASTER, null);
            CoordinateResponse mockCoordinate = new CoordinateResponse(BigDecimal.valueOf(35.1234), BigDecimal.valueOf(129.1234));

            CompanyRequest request = createCompanyRequest("스파르타 물류", "새로운 주소");

            given(companyService.getCompany(companyId)).willReturn(mockCompany);
            given(companyService.updateCompany(mockCompany, request, mockCoordinate)).willReturn(mockCompany);
            given(kakaoMapService.getCoordinate(request.baseAddress())).willReturn(mockCoordinate);
            given(hubClient.getHub(hubId)).willReturn(mockHubFeign);

            // when
            CompanyResponse.Update response = companyFacade.updateCompany(companyId, request, principal);

            // then
            assertThat(response).isNotNull();
            verify(companyService).getCompany(companyId);
            verify(companyService).updateCompany(mockCompany, request, mockCoordinate);
            verify(hubClient).getHub(hubId);
            verify(kakaoMapService).getCoordinate(request.baseAddress());
        }

        @Test
        @DisplayName("성공: MASTER - 허브 변경 가능")
        void test5() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_MASTER, null);

            UUID newHubId = UUID.randomUUID();
            CompanyRequest request = createCompanyRequest("스파르타 물류", newHubId);

            given(companyService.getCompany(companyId)).willReturn(mockCompany);
            given(companyService.updateCompany(mockCompany, request, null)).willReturn(mockCompany);
            given(hubClient.getHub(newHubId)).willReturn(ApiResponse.success(new HubResponse(newHubId, "새로운 허브")));

            // when
            CompanyResponse.Update response = companyFacade.updateCompany(companyId, request, principal);

            // then
            assertThat(response).isNotNull();
            verify(companyService).getCompany(companyId);
            verify(companyService).updateCompany(mockCompany, request, null);
            verify(hubClient).getHub(newHubId);
            verifyNoInteractions(kakaoMapService);
        }

        @Test
        @DisplayName("실패: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 불일치")
        void test6() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_HUB_MANAGER, UUID.randomUUID());
            given(companyService.getCompany(companyId)).willReturn(mockCompany);

            // when & then
            assertThatThrownBy(() -> companyFacade.updateCompany(companyId, request, principal))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_UPDATE_DENIED.getMessage());

            verify(companyService).getCompany(companyId);
            verify(companyService, never()).updateCompany(any(), any(), any());
            verifyNoInteractions(hubClient);
            verifyNoInteractions(kakaoMapService);
        }

        @Test
        @DisplayName("실패: COMPANY_MANAGER - 본인 업체 ID와 요청 업체 ID 불일치")
        void test7() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_COMPANY_MANAGER, UUID.randomUUID());
            given(companyService.getCompany(companyId)).willReturn(mockCompany);

            // when & then
            assertThatThrownBy(() -> companyFacade.updateCompany(companyId, request, principal))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_UPDATE_DENIED.getMessage());

            verify(companyService).getCompany(companyId);
            verify(companyService, never()).updateCompany(any(), any(), any());
            verifyNoInteractions(hubClient);
            verifyNoInteractions(kakaoMapService);
        }

        @Test
        @DisplayName("실패: HUB_MANAGER - 허브 변경 불가")
        void test8() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_HUB_MANAGER, UUID.randomUUID());
            given(companyService.getCompany(companyId)).willReturn(mockCompany);

            CompanyRequest request = createCompanyRequest("스파르타 물류", UUID.randomUUID());

            // when & then
            assertThatThrownBy(() -> companyFacade.updateCompany(companyId, request, principal))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_UPDATE_DENIED.getMessage());

            verify(companyService).getCompany(companyId);
            verify(companyService, never()).updateCompany(any(), any(), any());
            verifyNoInteractions(hubClient);
            verifyNoInteractions(kakaoMapService);
        }

        @Test
        @DisplayName("실패: COMPANY_MANAGER - 허브 변경 불가")
        void test9() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_COMPANY_MANAGER, UUID.randomUUID());
            given(companyService.getCompany(companyId)).willReturn(mockCompany);

            CompanyRequest request = createCompanyRequest("스파르타 물류", UUID.randomUUID());

            // when & then
            assertThatThrownBy(() -> companyFacade.updateCompany(companyId, request, principal))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_UPDATE_DENIED.getMessage());

            verify(companyService).getCompany(companyId);
            verify(companyService, never()).updateCompany(any(), any(), any());
            verifyNoInteractions(hubClient);
            verifyNoInteractions(kakaoMapService);
        }
    }

    @Test
    @DisplayName("업체 삭제 테스트")
    void deleteCompanyTest() {
        // given
        UUID deletedBy = UUID.randomUUID();
        CustomUserPrincipal principal = CustomUserPrincipal.of(deletedBy, UserRole.ROLE_MASTER, null);
        Company mockCompany = createCompany(companyId, createCompanyRequest("삭제 예정 물류"));
        given(companyService.deleteCompany(companyId, principal)).willReturn(mockCompany);

        // when
        CompanyResponse.Delete response = companyFacade.deleteCompany(companyId, principal);

        // then
        assertThat(response).isNotNull();
        verify(companyService).deleteCompany(companyId, principal);
        verify(productService).deleteProducts(companyId, deletedBy);
    }

    private CompanyRequest createCompanyRequest(String name) {
        return createCompanyRequest(name, hubId);
    }

    private CompanyRequest createCompanyRequest(String name, String baseAddress) {
        return createCompanyRequest(name, hubId, baseAddress);
    }

    private CompanyRequest createCompanyRequest(String name, UUID hubId) {
        return createCompanyRequest(name, hubId, "서울시 강남구 테헤란로311");
    }

    private CompanyRequest createCompanyRequest(String name, UUID hubId, String baseAddress) {
        return new CompanyRequest(
                name, CompanyType.SUPPLIER, "010-1234-5678", hubId,
                baseAddress, "3층 301호", "12345"
        );
    }

    private Company createCompany(UUID id, CompanyRequest request) {
        Company company =  Company.builder()
                .name(request.name())
                .type(request.type())
                .phone(request.phone())
                .hubId(request.hubId())
                .baseAddress(request.baseAddress())
                .detailAddress(request.detailAddress())
                .zipcode(request.zipcode())
                .latitude(BigDecimal.valueOf(37.503))
                .longitude(BigDecimal.valueOf(127.044))
                .build();
        ReflectionTestUtils.setField(company, "companyId", id); // 테스트용 업체 ID 강제 주입
        return company;
    }
}
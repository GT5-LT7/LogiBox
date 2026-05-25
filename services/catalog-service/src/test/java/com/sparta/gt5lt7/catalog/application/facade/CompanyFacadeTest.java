package com.sparta.gt5lt7.catalog.application.facade;

import com.sparta.gt5lt7.catalog.application.service.CompanyService;
import com.sparta.gt5lt7.catalog.application.service.KakaoMapService;
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
import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

        @Test
        @DisplayName("성공: MASTER - 허브 상관 없음")
        void test1() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_MASTER, null);
            given(hubClient.getHub(hubId)).willReturn(mockHub);
            given(kakaoMapService.getCoordinates(companyRequest.getBaseAddress())).willReturn(mockCoordinate);
            given(companyService.createCompanyEntity(companyRequest, mockCoordinate)).willReturn(mockCompany);

            // when
            CompanyResponse.Create response = companyFacade.createCompany(companyRequest, principal);

            // then
            assertThat(response.name()).isEqualTo(companyRequest.getName());
            verify(hubClient).getHub(hubId);
            verify(kakaoMapService).getCoordinates(companyRequest.getBaseAddress());
            verify(companyService).createCompanyEntity(companyRequest, mockCoordinate);
        }

        @Test
        @DisplayName("성공: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 일치")
        void test2() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_HUB_MANAGER, hubId);
            given(hubClient.getHub(companyRequest.getHubId())).willReturn(mockHub);
            given(kakaoMapService.getCoordinates(companyRequest.getBaseAddress())).willReturn(mockCoordinate);
            given(companyService.createCompanyEntity(companyRequest, mockCoordinate)).willReturn(mockCompany);

            // when
            CompanyResponse.Create response = companyFacade.createCompany(companyRequest, principal);

            // then
            assertThat(response).isNotNull();
            verify(hubClient).getHub(companyRequest.getHubId());
            verify(kakaoMapService).getCoordinates(companyRequest.getBaseAddress());
            verify(companyService).createCompanyEntity(companyRequest, mockCoordinate);
        }

        @Test
        @DisplayName("실패: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 불일치")
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

    @Nested
    @DisplayName("업체 조회 테스트")
    class GetCompanyTest {
        private final UUID createdBy = UUID.randomUUID();
        private final UUID updatedBy = UUID.randomUUID();

        Company mockCompany;

        @BeforeEach
        void setUp() {
            mockCompany = createCompany(companyId, createCompanyRequest("스파르타 물류"));
            ReflectionTestUtils.setField(mockCompany, "createdBy", createdBy);
            ReflectionTestUtils.setField(mockCompany, "updatedBy", updatedBy);
        }

        @Test
        @DisplayName("성공: 존재하는 업체 ID")
        void test1() {
            // given
            List<UserResponse> mockUserResponses = List.of(
                    new UserResponse(createdBy, "생성자"), new UserResponse(updatedBy, "수정자")
            );

            given(companyService.getCompany(companyId)).willReturn(mockCompany);
            given(hubClient.getHub(hubId)).willReturn(mockHub);
            given(userClient.getUsers(Set.of(createdBy, updatedBy))).willReturn(mockUserResponses);

            // when
            CompanyResponse.Detail response = companyFacade.getCompany(companyId);

            // then
            assertThat(response.info().name()).isEqualTo(mockCompany.getName());
            verify(companyService).getCompany(companyId);
            verify(hubClient).getHub(hubId);
            verify(userClient).getUsers(Set.of(createdBy, updatedBy));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 업체 ID - COMPANY_NOT_FOUND 예외 발생")
        void test2() {
            // given
            given(companyService.getCompany(companyId))
                    .willThrow(new BaseException(CompanyErrorCode.COMPANY_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> companyFacade.getCompany(companyId))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_NOT_FOUND.getMessage());


            verify(companyService).getCompany(companyId);
            verifyNoInteractions(hubClient);
            verifyNoInteractions(kakaoMapService);
        }
    }

    private CompanyRequest createCompanyRequest(String name) {
        return new CompanyRequest(
                name, CompanyType.SUPPLIER, "010-1234-5678", hubId,
                "서울시 강남구 테헤란로311", "3층 301호", "12345"
        );
    }

    private Company createCompany(UUID id, CompanyRequest request) {
        Company company =  Company.builder()
                .name(request.getName())
                .type(request.getType())
                .phone(request.getPhone())
                .hubId(request.getHubId())
                .baseAddress(request.getBaseAddress())
                .detailAddress(request.getDetailAddress())
                .zipcode(request.getZipcode())
                .latitude(BigDecimal.valueOf(37.503))
                .longitude(BigDecimal.valueOf(127.044))
                .build();
        ReflectionTestUtils.setField(company, "companyId", id); // 테스트용 업체 ID 강제 주입
        return company;
    }
}
package com.sparta.gt5lt7.catalog.application;

import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.domain.repository.CompanyRepository;
import com.sparta.gt5lt7.catalog.global.exception.CompanyErrorCode;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.UserClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.request.CompanyRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.response.HubUsageStatusResponse;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.common.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {
    @InjectMocks
    private CompanyService companyService;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ProductService productService;

    @Mock
    private HubClient hubClient;

    @Mock
    private UserClient userClient;

    private final UUID companyId = UUID.randomUUID();
    private final UUID hubId = UUID.randomUUID();

    private final HubResponse hubResponse  = new HubResponse(hubId, "서울 중앙 허브");

    @Nested
    @DisplayName("업체 생성 테스트")
    class CreateCompanyTest {
        private final CompanyRequest companyRequest = createCompanyRequest("스파르타 물류");
        private final Company mockCompany = createCompany(companyId, companyRequest);

        @Test
        @DisplayName("성공: MASTER - 허브 상관 없음")
        void test1() {
            // given
            List<String> roles = List.of("ROLE_MASTER");

            given(hubClient.getHub(companyRequest.getHubId())).willReturn(hubResponse);
            given(companyRepository.save(any(Company.class))).willReturn(mockCompany);

            // when
            CompanyResponse.Create response = companyService.createCompany(companyRequest, null, roles);

            // then
            assertThat(response.info().name()).isEqualTo(companyRequest.getName());
            assertThat(response.info().hub().name()).isEqualTo("서울 중앙 허브");

            // 레포지토리 저장 및 외부 클라이언트 호출 검증
            verify(hubClient).getHub(companyRequest.getHubId());
            verify(companyRepository).save(any(Company.class));
        }

        @Test
        @DisplayName("성공: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 일치")
        void test2() {
            // given
            List<String> roles = List.of("ROLE_HUB_MANAGER");

            given(hubClient.getHub(companyRequest.getHubId())).willReturn(hubResponse);
            given(companyRepository.save(any(Company.class))).willReturn(mockCompany);

            // when
            CompanyResponse.Create response = companyService.createCompany(companyRequest, hubId, roles); // hubId 일치

            // then
            assertThat(response).isNotNull();
            verify(hubClient).getHub(companyRequest.getHubId());
            verify(companyRepository).save(any(Company.class));
        }

        @Test
        @DisplayName("실패: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 불일치")
        void test3() {
            // given
            List<String> roles = List.of("ROLE_HUB_MANAGER");

            // when & then
            assertThatThrownBy(() -> companyService.createCompany(companyRequest, UUID.randomUUID(), roles))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_CREATE_DENIED.getMessage());

            // 레포지토리 저장 및 외부 클라이언트 호출 무시
            verify(hubClient, never()).getHub(any());
            verify(companyRepository, never()).save(any());
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
        Page<Company> mockPage = new PageImpl<>(List.of(mockCompany), pageable, 1);

        List<HubResponse> mockHubResponses = List.of(hubResponse);

        given(companyRepository.searchCompanies(keyword, type, hubId, pageable)).willReturn(mockPage);
        given(hubClient.getHubs(Set.of(hubId))).willReturn(mockHubResponses);

        // when
        PageResponse<CompanyResponse.Summary> response = companyService.searchCompanies(keyword, type, hubId, pageable);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).info().hub().name()).isEqualTo("서울 중앙 허브");
        verify(companyRepository).searchCompanies(keyword, type, hubId, pageable);
        verify(hubClient).getHubs(Set.of(hubId));
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

            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));
            given(hubClient.getHub(hubId)).willReturn(hubResponse);
            given(userClient.getUsers(Set.of(createdBy, updatedBy))).willReturn(mockUserResponses);

            // when
            CompanyResponse.Detail response = companyService.getCompany(companyId);

            // then
            assertThat(response.info().name()).isEqualTo("스파르타 물류");
            verify(companyRepository).findById(companyId);
            verify(hubClient).getHub(hubId);
            verify(userClient).getUsers(Set.of(createdBy, updatedBy));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 업체 ID - COMPANY_NOT_FOUND 예외 발생")
        void test2() {
            // given
            given(companyRepository.findById(companyId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> companyService.getCompany(companyId))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_NOT_FOUND.getMessage());


            verify(hubClient, never()).getHub(any());
            verify(userClient, never()).getUsers(any());
        }
    }

    @Test
    @DisplayName("허브 사용 여부 조회 테스트")
    void checkHubUsageTest() {
        // given
        given(companyRepository.existsByHubId(hubId)).willReturn(true);

        // when
        HubUsageStatusResponse response = companyService.checkHubUsage(hubId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.used()).isTrue();
        verify(companyRepository).existsByHubId(hubId);
    }

    @Nested
    @DisplayName("업체 수정 테스트")
    class UpdateCompanyTest {
        private final Company mockCompany = createCompany(companyId, createCompanyRequest("수정 예정 물류"));
        private final CompanyRequest companyRequest = createCompanyRequest("스파르타 물류");

        @Test
        @DisplayName("성공: MASTER - 허브 상관 없음")
        void test1() {
            // given
            List<String> roles = List.of("ROLE_MASTER");

            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));
            given(hubClient.getHub(hubId)).willReturn(hubResponse);

            // when
            CompanyResponse.Update response = companyService.updateCompany(companyId, companyRequest, null, roles);

            // then
            assertThat(response.info().name()).isEqualTo("스파르타 물류");
            verify(hubClient).getHub(hubId);
        }

        @Test
        @DisplayName("성공: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 일치")
        void test2() {
            // given
            List<String> roles = List.of("ROLE_HUB_MANAGER");

            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));
            given(hubClient.getHub(hubId)).willReturn(hubResponse);

            // when
            CompanyResponse.Update response = companyService.updateCompany(companyId, companyRequest, hubId, roles);

            // then
            assertThat(response).isNotNull();
            verify(hubClient).getHub(hubId);
        }

        @Test
        @DisplayName("성공: COMPANY_MANAGER - 본인 업체 ID와 요청 업체 ID 일치")
        void test3() {
            // given
            List<String> roles = List.of("ROLE_COMPANY_MANAGER");

            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));
            given(hubClient.getHub(hubId)).willReturn(hubResponse);

            // when
            CompanyResponse.Update response = companyService.updateCompany(companyId, companyRequest, hubId, roles);

            // then
            assertThat(response).isNotNull();
            verify(hubClient).getHub(hubId);
        }

        @Test
        @DisplayName("실패: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 불일치")
        void test4() {
            // given
            List<String> roles = List.of("ROLE_HUB_MANAGER");

            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));

            // when & then
            assertThatThrownBy(() -> companyService.updateCompany(companyId, companyRequest, UUID.randomUUID(), roles))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_UPDATE_DENIED.getMessage());
        }

        @Test
        @DisplayName("실패: COMPANY_MANAGER - 본인 업체 ID와 요청 업체 ID 불일치")
        void test5() {
            // given
            List<String> roles = List.of("ROLE_COMPANY_MANAGER");

            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));

            // when & then
            assertThatThrownBy(() -> companyService.updateCompany(companyId, companyRequest, UUID.randomUUID(), roles))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_UPDATE_DENIED.getMessage());
        }
    }

    @Nested
    @DisplayName("업체 삭제 테스트")
    class DeleteCompanyTest {
        private final Company mockCompany = createCompany(companyId, createCompanyRequest("수정 예정 물류"));
        private final UUID userOrHubId = hubId;

        @Test
        @DisplayName("성공: MASTER - 허브 상관 없음")
        void test1() {
            // given
            List<String> roles = List.of("ROLE_MASTER");

            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));

            // when
            CompanyResponse.Delete response = companyService.deleteCompany(companyId, userOrHubId, roles);

            // then
            assertThat(response.deletedAt()).isNotNull();
            verify(companyRepository).findById(companyId);
            verify(productService).deleteProducts(companyId, userOrHubId);
        }

        @Test
        @DisplayName("성공: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 일치")
        void test2() {
            // given
            List<String> roles = List.of("ROLE_HUB_MANAGER");
            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));

            // when
            CompanyResponse.Delete response = companyService.deleteCompany(companyId, userOrHubId, roles);

            // then
            assertThat(response.deletedAt()).isNotNull();

            verify(companyRepository).findById(companyId);
            verify(productService).deleteProducts(companyId, userOrHubId);
        }

        @Test
        @DisplayName("실패: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 일치")
        void test3() {
            // given
            List<String> roles = List.of("ROLE_HUB_MANAGER");
            UUID wrongUserId = UUID.randomUUID();

            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));

            // when & then
            assertThatThrownBy(() -> companyService.deleteCompany(companyId, wrongUserId, roles))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_DELETE_DENIED.getMessage());
            verify(productService, never()).deleteProducts(any(UUID.class), any(UUID.class));
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
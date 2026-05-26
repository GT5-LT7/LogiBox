package com.sparta.gt5lt7.catalog.application.service;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.domain.repository.CompanyRepository;
import com.sparta.gt5lt7.catalog.global.exception.CompanyErrorCode;
import com.sparta.gt5lt7.catalog.presentation.dto.request.CompanyRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CoordinateResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.response.HubUsageStatusResponse;
import com.sparta.gt5lt7.common.exception.BaseException;
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

    private final UUID companyId = UUID.randomUUID();
    private final UUID hubId = UUID.randomUUID();

    @Test
    @DisplayName("업체 엔티티 생성 테스트")
    void createCompanyTest() {
        // given
        CompanyRequest request = createCompanyRequest("스파르타 물류");
        CoordinateResponse mockCoordinate = new CoordinateResponse(new BigDecimal("37.5"), new BigDecimal("127.0"));
        given(companyRepository.save(any(Company.class))).willReturn(createCompany(companyId, request));

        // when
        Company company = companyService.createCompany(request, mockCoordinate);

        // then
        assertThat(company.getName()).isEqualTo(request.getName());
        verify(companyRepository).save(any(Company.class));
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

        given(companyRepository.searchCompanies(keyword, type, hubId, pageable)).willReturn(mockPage);

        // when
        Page<Company> companyPage = companyService.searchCompanies(keyword, type, hubId, pageable);

        // then
        assertThat(companyPage.getContent()).hasSize(1);
        assertThat(companyPage.getContent().get(0).getName()).isEqualTo(mockCompany.getName());
        verify(companyRepository).searchCompanies(keyword, type, hubId, pageable);
    }

    @Test
    @DisplayName("허브 사용 여부 조회 테스트")
    void checkHubUsageTest() {
        // given
        given(companyRepository.existsByHubId(hubId)).willReturn(true);

        // when
        boolean isCompanyInUse = companyService.checkHubUsage(hubId);

        // then
        assertThat(isCompanyInUse).isTrue();
        verify(companyRepository).existsByHubId(hubId);
    }

    @Nested
    @DisplayName("업체 수정 테스트")
    class UpdateCompanyTest {
        private final Company mockCompany = createCompany(companyId, createCompanyRequest("수정 예정 물류"));
        private final CompanyRequest request = createCompanyRequest("새로운 이름");

        @Test
        @DisplayName("성공: 좌표 정보가 null이면 위·경도 유지")
        void testUpdateWithoutCoordinate() {
            // given
            given(companyRepository.save(any(Company.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Company company = companyService.updateCompany(mockCompany, request, null);

            // then
            assertThat(company.getName()).isEqualTo(request.getName());
            assertThat(company.getLatitude()).isEqualTo(BigDecimal.valueOf(37.503));
            assertThat(company.getLongitude()).isEqualTo(BigDecimal.valueOf(127.044));
        }

        @Test
        @DisplayName("성공: 좌표 정보가 null이 아니면 위·경도 갱신")
        void testUpdateWithCoordinate() {
            // given
            CoordinateResponse mockCoordinate = new CoordinateResponse(BigDecimal.valueOf(35.1234), BigDecimal.valueOf(129.1234));
            given(companyRepository.save(any(Company.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Company company = companyService.updateCompany(mockCompany, request, mockCoordinate);

            // then
            assertThat(company.getName()).isEqualTo(request.getName());
            assertThat(company.getLatitude()).isEqualTo(mockCoordinate.latitude());
            assertThat(company.getLongitude()).isEqualTo(mockCoordinate.longitude());
        }
    }

    @Nested
    @DisplayName("업체 삭제 테스트")
    class DeleteCompanyTest {
        private final Company mockCompany = createCompany(companyId, createCompanyRequest("삭제 예정 물류"));

        @Test
        @DisplayName("성공: MASTER - 허브 상관 없음")
        void test1() {
            // given
            UUID deletedBy = UUID.randomUUID();
            CustomUserPrincipal principal = CustomUserPrincipal.of(deletedBy, UserRole.ROLE_MASTER, null);
            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));

            // when
            Company company = companyService.deleteCompany(companyId, principal);

            // then
            assertThat(company.getDeletedAt()).isNotNull();
            verify(companyRepository).findById(companyId);
        }

        @Test
        @DisplayName("성공: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 일치")
        void test2() {
            // given
            UUID deletedBy = UUID.randomUUID();
            CustomUserPrincipal principal = CustomUserPrincipal.of(deletedBy, UserRole.ROLE_HUB_MANAGER, hubId);
            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));

            // when
            Company company = companyService.deleteCompany(companyId, principal);

            // then
            assertThat(company.getDeletedAt()).isNotNull();
            verify(companyRepository).findById(companyId);
        }

        @Test
        @DisplayName("실패: HUB_MANAGER - 담당 허브 ID와 요청 허브 ID 불일치")
        void test3() {
            // given
            CustomUserPrincipal principal = CustomUserPrincipal.of(UUID.randomUUID(), UserRole.ROLE_HUB_MANAGER, UUID.randomUUID());
            given(companyRepository.findById(companyId)).willReturn(Optional.of(mockCompany));

            // when & then
            assertThatThrownBy(() -> companyService.deleteCompany(companyId, principal))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CompanyErrorCode.COMPANY_DELETE_DENIED.getMessage());

            verify(companyRepository).findById(companyId);
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
package com.sparta.gt5lt7.catalog.domain.repository;

import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.global.config.QueryDslConfig;
import com.sparta.gt5lt7.catalog.global.config.TestJpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestJpaConfig.class, QueryDslConfig.class})
class CompanyRepositoryImplTest {
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final UUID hubAId = UUID.randomUUID();
    private final UUID hubBId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createCompany("삼성전자", CompanyType.SUPPLIER, hubAId);
        createCompany("삼성SDS", CompanyType.RECEIVER, hubAId);
        createCompany("LG전자", CompanyType.SUPPLIER, hubBId);
        createCompany("네이버", CompanyType.RECEIVER, hubBId);

        // Soft Delete 검증용
        Company deletedCompany = createCompany("카카오(삭제됨)", CompanyType.SUPPLIER, hubAId);
        deletedCompany.softDelete(UUID.randomUUID());

        entityManager.flush();
        entityManager.clear();
    }

    // ==========================================
    // 🟢 성공 케이스
    // ==========================================
    @Test
    @DisplayName("성공: 조건 없이 조회 시 삭제된 데이터를 제외한 4건 조회")
    void test1() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Company> result = companyRepository.searchCompanies(null, null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("성공: 'sds' 검색 시 대소문자 구분 없이 매칭되는 업체만 조회")
    void test2() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Company> result = companyRepository.searchCompanies("sds", null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(Company::getName)
                .containsExactly("삼성SDS");
    }

    @Test
    @DisplayName("성공: 'SUPPLIER' 필터링 적용 시 공급업체만 조회")
    void test3() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Company> result = companyRepository.searchCompanies(null, CompanyType.SUPPLIER, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Company::getName)
                .containsExactlyInAnyOrder("삼성전자", "LG전자");
    }

    @Test
    @DisplayName("성공: 허브 ID 필터링 시 해당 허브에 속한 업체만 조회")
    void test4() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Company> result = companyRepository.searchCompanies(null, null, hubAId, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("성공: 모든 조건 적용 시 해당 조건을 모두 만족하는 업체만 조회")
    void test5() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Company> result = companyRepository.searchCompanies("삼성", CompanyType.SUPPLIER, hubAId, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("삼성전자");
    }


    // ==========================================
    // 🔴 실패 및 예외 케이스
    // ==========================================
    @Test
    @DisplayName("실패: Soft Delete된 업체는 어떤 조건에서도 조회 불가")
    void test6() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Company> allResult = companyRepository.searchCompanies(null, null, null, pageable);
        Page<Company> keywordResult = companyRepository.searchCompanies("카카오", CompanyType.SUPPLIER, hubAId, pageable);

        // then
        assertThat(allResult.getContent())
                .extracting(Company::getName)
                .doesNotContain("카카오(삭제됨)");
        assertThat(keywordResult.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("실패: 일치하는 검색 결과가 없으면 빈 페이지 반환")
    void test7() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Company> result = companyRepository.searchCompanies("존재하지않는업체", null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("예외: 키워드에 공백 문자열이 들어오면 검색 조건에서 제외")
    void test8() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Company> result = companyRepository.searchCompanies("   ", null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    // ==========================================
    // 🛠️ 편의 메서드
    // ==========================================
    private Company createCompany(String name, CompanyType type, UUID hubId) {
        Company company = Company.builder()
                .name(name)
                .type(type)
                .hubId(hubId)
                .phone("010-1234-5678")
                .baseAddress("서울시 강남구 테헤란로311")
                .detailAddress("3층 301호")
                .zipcode("12345")
                .latitude(BigDecimal.valueOf(37.5665))
                .longitude(BigDecimal.valueOf(126.9780))
                .build();
        return entityManager.persist(company);
    }
}
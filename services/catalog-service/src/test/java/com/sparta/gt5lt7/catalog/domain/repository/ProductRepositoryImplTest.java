package com.sparta.gt5lt7.catalog.domain.repository;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.domain.entity.*;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestJpaConfig.class, QueryDslConfig.class})
class ProductRepositoryImplTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID companyId;
    private UUID categoryId;

    private final UUID hubAId = UUID.randomUUID();
    private final UUID hubBId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Company companyA = createCompany("삼성전자", hubAId);
        Company companyB = createCompany("LG전자", hubBId);
        Company companyC = createCompany("CJ제일제당", hubAId);

        companyId = companyA.getCompanyId();

        Category categoryA = createCategory("디지털/가전");
        Category categoryB = createCategory("가공식품");

        categoryId = categoryA.getCategoryId();

        createProduct("Samsung 모니터", companyA, categoryA, ProductStatus.ON_SALE);
        createProduct("SAMSUNG 키보드(숨김)", companyA, categoryA, ProductStatus.HIDDEN);
        createProduct("LG 울트라기어 모니터(숨김)", companyB, categoryA, ProductStatus.HIDDEN);
        createProduct("비비고 왕교자 만두", companyC, categoryB, ProductStatus.ON_SALE);
        createProduct("마우스", companyA, categoryA, ProductStatus.SOLD_OUT);

        // Soft Delete 검증용
        Product deletedProduct = createProduct("비비고 김치(삭제)", companyC, categoryB, ProductStatus.ON_SALE);
        deletedProduct.softDelete(UUID.randomUUID());

        entityManager.flush();
        entityManager.clear();
    }

    // ==========================================
    // 🟢 성공 케이스
    // ==========================================
    @Test
    @DisplayName("성공: 조건 없이 조회 시 삭제된 데이터를 제외한 5건 조회")
    void test1() {
        // given
        CustomUserPrincipal master = createPrincipal(UserRole.ROLE_MASTER, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productRepository.searchProducts(
                null, false, null, null, null, pageable, master
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getContent()).hasSize(5);
    }

    @Test
    @DisplayName("성공: 'samsung' 검색 시 대소문자 구분 없이 매칭되는 상품만 조회")
    void test2() {
        // given
        CustomUserPrincipal master = createPrincipal(UserRole.ROLE_MASTER, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productRepository.searchProducts(
                "samsung", false, null, null, null, pageable, master
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Samsung 모니터", "SAMSUNG 키보드(숨김)");
    }

    @Test
    @DisplayName("성공: 업체 ID 필터링 시 해당 업체에 속한 상품만 조회")
    void test3() {
        // given
        CustomUserPrincipal master = createPrincipal(UserRole.ROLE_MASTER, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productRepository.searchProducts(
                null, false, companyId, null, null, pageable, master
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("성공: 허브 ID 필터링 시 해당 허브에 속한 상품만 조회")
    void test4() {
        // given
        CustomUserPrincipal master = createPrincipal(UserRole.ROLE_MASTER, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productRepository.searchProducts(
                null, false, null, hubAId, null, pageable, master
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("성공: 카테고리 ID 필터링 시 해당 카테고리에 속한 상품만 조회")
    void test5() {
        // given
        CustomUserPrincipal master = createPrincipal(UserRole.ROLE_MASTER, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productRepository.searchProducts(
                null, false, null, null, categoryId, pageable, master
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("성공: 판매 중 필터링 시 판매 중인 상품만 조회")
    void test6() {
        // given
        CustomUserPrincipal master = createPrincipal(UserRole.ROLE_MASTER, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productRepository.searchProducts(
                null, true, null, null, null, pageable, master
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("성공: HUB_MANAGER - 본인 허브 상품의 숨김 상품 조회 가능")
    void test7() {
        // given
        CustomUserPrincipal hunManager = createPrincipal(UserRole.ROLE_HUB_MANAGER, hubAId);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productRepository.searchProducts(
                null, false, null, null, null, pageable, hunManager
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).extracting(Product::getName)
                .contains("SAMSUNG 키보드(숨김)")
                .doesNotContain("LG 울트라기어 모니터(숨김)");
    }

    @Test
    @DisplayName("성공: 모든 조건 적용 시 해당 조건을 모두 만족하는 상품만 조회")
    void test8() {
        // given
        CustomUserPrincipal master = createPrincipal(UserRole.ROLE_MASTER, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productRepository.searchProducts(
                "모니터", true, companyId, hubAId, categoryId, pageable, master
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Samsung 모니터");
    }

    // ==========================================
    // 🔴 실패 및 예외 케이스
    // ==========================================
    @Test
    @DisplayName("실패: 일치하는 검색 결과가 없으면 빈 페이지 반환")
    void test9() {
        // given
        CustomUserPrincipal master = createPrincipal(UserRole.ROLE_MASTER, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productRepository.searchProducts(
                "없는 상품", false, null, null, null, pageable, master
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("실패: 모든 조건 적용 시 조건을 하나라도 만족하지 않으면 빈 페이지 반환")
    void test10() {
        // given
        CustomUserPrincipal master = createPrincipal(UserRole.ROLE_MASTER, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productRepository.searchProducts(
                "모니터", true, companyId, hubBId, categoryId, pageable, master
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("예외: 키워드에 공백 문자열이 들어오면 검색 조건에서 제외")
    void test11() {
        // given
        CustomUserPrincipal master = createPrincipal(UserRole.ROLE_MASTER, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productRepository.searchProducts(
                "   ", false, null, null, null, pageable, master
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(5);
    }

    // ==========================================
    // 🛠️ 편의 메서드
    // ==========================================
    private CustomUserPrincipal createPrincipal(UserRole role, UUID managementId) {
        return CustomUserPrincipal.of(UUID.randomUUID(), role, managementId);
    }

    private Product createProduct(String name, Company company, Category category, ProductStatus status) {
        Product product = Product.builder()
                .name(name)
                .company(company)
                .category(category)
                .price(10000L)
                .quantity(100)
                .build();
        ReflectionTestUtils.setField(product, "status", status); // 테스트용 상태 강제 주입
        return entityManager.persist(product);
    }

    private Company createCompany(String name, UUID hubId) {
        Company company = Company.builder()
                .name(name)
                .type(CompanyType.SUPPLIER)
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

    private Category createCategory(String name) {
        Category category = Category.builder().name(name).build();
        return entityManager.persist(category);
    }
}
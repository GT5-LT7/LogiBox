package com.sparta.gt5lt7.catalog.domain.repository;

import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.global.config.QueryDslConfig;
import com.sparta.gt5lt7.catalog.global.config.TestJpaConfig;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestJpaConfig.class, QueryDslConfig.class})
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("상품 조회 테스트")
    void findByIdWithCompanyTest() {
        // given
        Company company = createCompany();
        Product product = createProduct(company);

        flushAndClear();

        // when
        Optional<Product> result = productRepository.findByIdWithCompany(product.getProductId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(product.getName());
        assertThat(result.get().getCompany().getName()).isEqualTo(company.getName());
    }

    @Test
    @DisplayName("업체별 상품 연쇄 삭제 테스트")
    void softDeleteByProductIdTest() {
        // given
        Company targetCompany = createCompany();
        Company otherCompany = createCompany();

        UUID targetCompanyId = targetCompany.getCompanyId();

        // 삭제 대상
        Product targetProduct1 = createProduct(targetCompany);
        Product targetProduct2 = createProduct(targetCompany);

        // 제외 대상: 이미 삭제된 상품
        Product deletedProduct = createProduct(targetCompany);

        // 제외 대상: 타 업체 상품
        Product otherCompanyProduct = createProduct(otherCompany);

        // DB 반영한 후 Soft Delete 처리
        entityManager.flush();
        deletedProduct.softDelete(UUID.randomUUID());

        flushAndClear();

        UUID deletedBy = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 5, 24, 17, 0);

        // when
        productRepository.softDeleteByProductId(targetCompanyId, deletedBy, now);
        entityManager.clear(); // 벌크 연산 후 컨텍스트 초기화

        // then
        // EntityManager를 사용해 SQL Restriction 필터 우회
        targetProduct1 = (Product) entityManager.getEntityManager()
                .createNativeQuery("SELECT * FROM p_products WHERE product_id = ?1", Product.class)
                .setParameter(1, targetProduct1.getProductId())
                .getSingleResult();
        targetProduct2 = (Product) entityManager.getEntityManager()
                .createNativeQuery("SELECT * FROM p_products WHERE product_id = ?1", Product.class)
                .setParameter(1, targetProduct2.getProductId())
                .getSingleResult();

        deletedProduct = (Product) entityManager.getEntityManager()
                .createNativeQuery("SELECT * FROM p_products WHERE product_id = ?1", Product.class)
                .setParameter(1, deletedProduct.getProductId())
                .getSingleResult();

        otherCompanyProduct = productRepository.findById(otherCompanyProduct.getProductId()).orElseThrow();

        assertThat(targetProduct1.getDeletedAt()).isEqualTo(now);
        assertThat(targetProduct1.getDeletedBy()).isEqualTo(deletedBy);
        assertThat(targetProduct2.getDeletedAt()).isEqualTo(now);
        assertThat(targetProduct2.getDeletedBy()).isEqualTo(deletedBy);

        assertThat(deletedProduct.getDeletedBy()).isNotEqualTo(deletedBy);

        assertThat(otherCompanyProduct.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("상품 재고 차감을 위한 비관적 락 조회 테스트")
    void findAllByIdsInForOrderTest() {
        // given
        Company company = createCompany();

        Product product1 = createProduct(company);
        Product product2 = createProduct(company);
        Product product3 = createProduct(company);

        product1.updateStatus(ActionType.HIDE);
        product2.updateStatus(ActionType.STOP);

        UUID productId1 = product1.getProductId();
        UUID productId2 = product2.getProductId();
        UUID productId3 = product3.getProductId();

        flushAndClear();

        List<UUID> requests = List.of(productId1, productId2, productId3);

        // when
        List<Product> result = productRepository.findAllByIdsInForOrder(requests);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(productId3);
    }

    @Test
    @DisplayName("상품 재고 원복을 위한 비관적 락 조회 테스트")
    void findAllByIdInForRollbackTest() {
        // given
        Company company = createCompany();

        Product product1 = createProduct(company);
        Product product2 = createProduct(company);
        Product product3 = createProduct(company);

        product1.updateStatus(ActionType.HIDE);
        product1.updateStatus(ActionType.STOP);

        UUID productId1 = product1.getProductId();
        UUID productId2 = product2.getProductId();
        UUID productId3 = product3.getProductId();

        flushAndClear();

        List<UUID> requests = List.of(productId1, productId2, productId3);

        // when
        List<Product> result = productRepository.findAllByIdInsForRollback(requests);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Product::getProductId).containsExactlyInAnyOrder(productId1, productId2, productId3);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Product createProduct(Company company) {
        Product product = Product.builder()
                .name("테스트 상품")
                .company(company)
                .price(10000L)
                .quantity(100)
                .build();
        return entityManager.persist(product);
    }

    private Company createCompany() {
        Company company = Company.builder()
                .name("테스트 업체")
                .type(CompanyType.SUPPLIER)
                .hubId(UUID.randomUUID())
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
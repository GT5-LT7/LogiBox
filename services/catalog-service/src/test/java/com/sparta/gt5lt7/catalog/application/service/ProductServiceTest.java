package com.sparta.gt5lt7.catalog.application.service;

import com.sparta.gt5lt7.catalog.domain.entity.ProductStatus;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.domain.repository.ProductRepository;
import com.sparta.gt5lt7.catalog.global.exception.ProductErrorCode;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ActionType;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    private final UUID productId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID hubId = UUID.randomUUID();

    @Nested
    @DisplayName("상품 생성 테스트")
    class CreateProductTest {
        private final CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        private final Company mockCompany = createCompany(companyId, hubId);
        private final ProductRequest.Create request = new ProductRequest.Create(
                "테스트 상품", "설명", companyId, 1500000L, 100
        );

        @Test
        @DisplayName("성공: 업체 생성 권한 있음")
        void test1() {
            // given
            Product mockProduct = createProduct(productId, request.name(), mockCompany, request.price(), request.quantity());
            given(principal.isAccessibleHub(hubId)).willReturn(true);
            given(productRepository.save(any(Product.class))).willReturn(mockProduct);

            // when
            Product product = productService.createProduct(mockCompany, request, principal);

            // then
            assertThat(product.getName()).isEqualTo(request.name());
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("실패: 업체 생성 권한 없음")
        void test2() {
            // given
            given(principal.isAccessibleHub(hubId)).willReturn(false);
            given(principal.isAccessibleCompany(request.companyId())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> productService.createProduct(mockCompany, request, principal))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(ProductErrorCode.PRODUCT_CREATE_DENIED.getMessage());
        }
    }

    @Test
    @DisplayName("상품 목록 조회 테스트")
    void searchProductsTest() {
        // given
        String keyword = "테스트";
        Boolean salesOnly = true;
        Pageable pageable = PageRequest.of(0, 10);

        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        Company mockCompany = createCompany(companyId, hubId);
        Product mockProduct = createProduct(productId, "테스트 상품", mockCompany, 1500000L, 100);
        Page<Product> mockPage = new PageImpl<>(List.of(mockProduct), pageable, 1);

        given(productRepository.searchProducts(keyword, salesOnly, companyId, hubId, pageable, principal)).willReturn(mockPage);

        // when
        Page<Product> productPage = productService.searchProducts(keyword, salesOnly, companyId, hubId, pageable, principal);

        // then
        assertThat(productPage.getContent()).hasSize(1);
        assertThat(productPage.getContent().get(0).getName()).isEqualTo(mockProduct.getName());
        verify(productRepository).searchProducts(keyword, salesOnly, companyId, hubId, pageable, principal);
    }

    @Nested
    @DisplayName("상품 수정 테스트")
    class UpdateProductTest {
        private final CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        private final Company mockCompany = createCompany(companyId, hubId);
        private final Product mockProduct = createProduct(
                productId, "테스트 상품", mockCompany, 1500000L, 100
        );
        private final ProductRequest.Update request = new ProductRequest.Update(
                "테스트 상품(수정)", "설명", 2500000L
        );

        @Test
        @DisplayName("성공: 상품 수정 권한 있음")
        void test1() {
            // given
            given(principal.isAccessibleHub(hubId)).willReturn(true);
            given(productRepository.findByIdWithCompany(productId)).willReturn(Optional.of(mockProduct));

            // when
            Product product = productService.updateProduct(productId, request, principal);

            // then
            assertThat(product.getName()).isEqualTo(request.name());
            assertThat(product.getDescription()).isEqualTo(request.description());
            assertThat(product.getPrice()).isEqualTo(request.price());
        }

        @Test
        @DisplayName("실패: 상품 수정 권한 없음")
        void test2() {
            // given
            given(productRepository.findByIdWithCompany(productId)).willReturn(Optional.of(mockProduct));
            given(principal.isAccessibleHub(hubId)).willReturn(false);
            given(principal.isAccessibleCompany(companyId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(productId, request, principal))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(ProductErrorCode.PRODUCT_UPDATE_DENIED.getMessage());
        }
    }

    @Nested
    @DisplayName("상품 상태 변경 테스트")
    class UpdateProductStatusTest {
        private final CustomUserPrincipal principal = mock(CustomUserPrincipal.class);

        @Test
        @DisplayName("성공: 상품 상태 변경 권한 있음")
        void test1() {
            // given
            ActionType action = ActionType.HIDE;
            Company mockCompany = createCompany(companyId, hubId);
            Product mockProduct = createProduct(productId, "테스트 상품", mockCompany, 1000L, 10);

            given(principal.isAccessibleHub(hubId)).willReturn(true);
            given(productRepository.findByIdWithCompany(productId)).willReturn(Optional.of(mockProduct));

            // when
            Product product = productService.updateProductStatus(productId, action, principal);

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
        }

        @Test
        @DisplayName("실패: MASTER가 아니면 판매중단 불가")
        void test2() {
            // given
            ActionType action = ActionType.STOP;
            given(principal.isMaster()).willReturn(false);

            // when & then
            assertThatThrownBy(() -> productService.updateProductStatus(productId, action, principal))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(ProductErrorCode.PRODUCT_UPDATE_DENIED.getMessage());
        }
    }

    @Nested
    @DisplayName("상품 재고 변경 테스트")
    class UpdateProductQuantityTest {
        @Test
        @DisplayName("성공: 재고 수량 증가 또는 남은 수량 0 이상")
        void test1() {
            // given
            Company mockCompany = createCompany(companyId, hubId);
            Product mockProduct = createProduct(productId, "테스트 상품", mockCompany, 1000L, 10);
            given(productRepository.findByIdForUpdate(productId)).willReturn(mockProduct);

            // when
            Product product = productService.updateProductQuantity(productId, -5);

            // then
            assertThat(product.getQuantity()).isEqualTo(5);
            verify(productRepository).findByIdForUpdate(productId);
        }

        @Test
        @DisplayName("실패: 재고 차감 시 수량 0 미만")
        void test2() {
            // given
            Company mockCompany = createCompany(companyId, hubId);
            Product mockProduct = createProduct(productId, "상품", mockCompany, 1000L, 10);
            given(productRepository.findByIdForUpdate(productId)).willReturn(mockProduct);

            // when & then
            assertThatThrownBy(() -> productService.updateProductQuantity(productId, -15))
                    .isInstanceOf(com.sparta.gt5lt7.common.exception.BaseException.class)
                    .hasMessageContaining(ProductErrorCode.OUT_OF_STOCK.getMessage());
        }
    }

    @Test
    @DisplayName("상품 ID 기반 상품 목록 조회 테스트")
    void findAllByIdsTest() {
        // given
        ProductRequest.StockItem item = new ProductRequest.StockItem(productId, 2);
        Company mockCompany = createCompany(companyId, hubId);
        Product mockProduct = createProduct(productId, "테스트 상품", mockCompany, 1000L, 10);

        given(productRepository.findAllById(List.of(productId))).willReturn(List.of(mockProduct));

        // when
        List<Product> products = productService.findAllByIds(List.of(item));

        // then
        assertThat(products).hasSize(1);
        verify(productRepository).findAllById(List.of(productId));
    }

    @Nested
    @DisplayName("주문 상품 재고 변경 테스트")
    class UpdateProductQuantityForOrderTest {
        @Test
        @DisplayName("성공: 상품 재고 차감")
        void test1() {
            // given
            UUID product2Id = UUID.randomUUID();

            Company mockCompany = createCompany(companyId, hubId);
            Product product1 = createProduct(productId, "테스트 상품1", mockCompany, 1000L, 10);
            Product product2 = createProduct(product2Id, "테스트 상품2", mockCompany, 2000L, 20);

            List<UUID> productIds = List.of(productId, product2Id);
            Map<UUID, Integer> quantityMap = Map.of(productId, -3, product2Id, -5);

            given(productRepository.findAllByIdsInForOrder(productIds)).willReturn(List.of(product1, product2));

            // when
            List<Product> products = productService.updateProductQuantityForOrder(productIds, quantityMap, false);

            // then
            assertThat(products.size()).isEqualTo(2);
            assertThat(product1.getQuantity()).isEqualTo(7);
            assertThat(product2.getQuantity()).isEqualTo(15);
            verify(productRepository).findAllByIdsInForOrder(productIds);
            verify(productRepository, never()).findAllByIdInsForRollback(productIds);
        }

        @Test
        @DisplayName("성공: 상품 재고 원복")
        void test2() {
            // given
            UUID product2Id = UUID.randomUUID();

            Company mockCompany = createCompany(companyId, hubId);
            Product product1 = createProduct(productId, "테스트 상품1", mockCompany, 1000L, 10);
            Product product2 = createProduct(product2Id, "테스트 상품2", mockCompany, 2000L, 20);

            List<UUID> productIds = List.of(productId, product2Id);
            Map<UUID, Integer> quantityMap = Map.of(productId, 3, product2Id, 5);

            given(productRepository.findAllByIdInsForRollback(productIds)).willReturn(List.of(product1, product2));

            // when
            List<Product> products = productService.updateProductQuantityForOrder(productIds, quantityMap, true);

            // then
            assertThat(products.size()).isEqualTo(2);
            assertThat(product1.getQuantity()).isEqualTo(13);
            assertThat(product2.getQuantity()).isEqualTo(25);
            verify(productRepository).findAllByIdInsForRollback(productIds);
            verify(productRepository, never()).findAllByIdsInForOrder(productIds);
        }
    }

    @Nested
    @DisplayName("Redis 롤백 기록 제어 테스트")
    class RedisRollbackHistoryTest {
        private final String redisKey = "rollback:order:123";

        @Test
        @DisplayName("성공: 최초 요청 시 Redis 임시 선점 및 true 반환")
        void reserveRollbackHistoryTest1() {
            // given
            @SuppressWarnings("unchecked")
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.setIfAbsent(eq(redisKey), eq("processing"), eq(30L), eq(TimeUnit.SECONDS)))
                    .willReturn(true);

            // when
            boolean result = productService.reserveRollbackHistory(redisKey);

            // then
            assertThat(result).isTrue();
            verify(valueOperations).setIfAbsent(eq(redisKey), eq("processing"), eq(30L), eq(TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("성공: 이미 처리 중이거나 완료된 요청 시 false 반환")
        void reserveRollbackHistoryTest2() {
            // given
            @SuppressWarnings("unchecked")
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.setIfAbsent(eq(redisKey), eq("processing"), eq(30L), eq(TimeUnit.SECONDS)))
                    .willReturn(false);

            // when
            boolean result = productService.reserveRollbackHistory(redisKey);

            // then
            assertThat(result).isFalse();
            verify(valueOperations).setIfAbsent(eq(redisKey), eq("processing"), eq(30L), eq(TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("성공: 롤백 성공 시 Redis 기록 확정")
        void confirmRollbackHistoryTest() {
            // given
            @SuppressWarnings("unchecked")
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            doNothing().when(valueOperations).set(eq(redisKey), eq("processed"), anyLong(), eq(TimeUnit.SECONDS));

            // when
            productService.confirmRollbackHistory(redisKey);

            // then
            verify(redisTemplate).opsForValue();
            verify(valueOperations).set(eq(redisKey), eq("processed"), anyLong(), eq(TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("성공: 예외 발생 시 Redis 키 삭제")
        void clearRollbackHistoryTest() {
            // given
            given(redisTemplate.delete(redisKey)).willReturn(true);

            // when
            productService.clearRollbackHistory(redisKey);

            // then
            verify(redisTemplate).delete(redisKey);
        }
    }

    @Nested
    @DisplayName("상품 삭제 테스트")
    class DeleteProductTest {
        private final CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        private final Company mockCompany = createCompany(companyId, hubId);
        private final Product mockProduct = createProduct(productId, "테스트 상품", mockCompany, 1000L, 10);

        @Test
        @DisplayName("성공: 상품 삭제 권한 있음")
        void test1() {
            // given
            given(principal.isAccessibleHub(hubId)).willReturn(true);
            given(principal.userId()).willReturn(UUID.randomUUID());
            given(productRepository.findByIdWithCompany(productId)).willReturn(Optional.of(mockProduct));

            // when
            Product product = productService.deleteProduct(productId, principal);

            // then
            assertThat(product.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패: 상품 삭제 권한 없음")
        void test2() {
            // given
            given(principal.isAccessibleHub(hubId)).willReturn(false);
            given(productRepository.findByIdWithCompany(productId)).willReturn(Optional.of(mockProduct));

            // when & then
            assertThatThrownBy(() -> productService.deleteProduct(productId, principal))
                    .isInstanceOf(com.sparta.gt5lt7.common.exception.BaseException.class)
                    .hasMessageContaining(ProductErrorCode.PRODUCT_DELETE_DENIED.getMessage());
        }
    }

    @Test
    @DisplayName("상품 연쇄 삭제 테스트")
    void deleteProductsTest() {
        // given
        UUID deletedBy = UUID.randomUUID();

        // when
        productService.deleteProducts(companyId, deletedBy);

        // then
        verify(productRepository).softDeleteByProductId(eq(companyId), eq(deletedBy), any(LocalDateTime.class));
    }

    private Company createCompany(UUID companyId, UUID hubId) {
        Company company = Company.builder()
                .name("테스트 업체")
                .hubId(hubId)
                .baseAddress("서울시 강남구 테헤란로311")
                .build();
        ReflectionTestUtils.setField(company, "companyId", companyId);
        return company;
    }

    private Product createProduct(UUID productId, String name, Company company, Long price, Integer quantity) {
        Product product = Product.builder()
                .name(name)
                .company(company)
                .price(price)
                .quantity(quantity)
                .build();
        ReflectionTestUtils.setField(product, "productId", productId);
        return product;
    }
}
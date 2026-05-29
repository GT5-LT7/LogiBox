package com.sparta.gt5lt7.catalog.application.facade;

import com.sparta.gt5lt7.common.exception.CommonErrorCode;
import com.sparta.gt5lt7.common.dto.ApiResponse;
import com.sparta.gt5lt7.catalog.domain.entity.ProductStatus;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.common.dto.PageResponse;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.application.service.CompanyService;
import com.sparta.gt5lt7.catalog.application.service.ProductService;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.global.exception.ProductErrorCode;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.UserClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.ProductResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFacadeTest {
    @InjectMocks
    private ProductFacade productFacade;

    @Mock
    private ProductService productService;

    @Mock
    private CompanyService companyService;

    @Mock
    private HubClient hubClient;

    @Mock
    private UserClient userClient;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    private final UUID productId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID hubId = UUID.randomUUID();

    @Test
    @DisplayName("상품 생성 테스트")
    void createProductTest() {
        // given
        ProductRequest.Create request = new ProductRequest.Create(
                "테스트 상품", "설명", companyId, 1500L, 10
        );
        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        Company mockCompany = createCompany(companyId, hubId);
        Product mockProduct = createProduct(productId, "테스트 상품", mockCompany, 1500L, 10);

        given(companyService.getCompany(companyId)).willReturn(mockCompany);
        given(productService.createProduct(mockCompany, request, principal)).willReturn(mockProduct);

        // when
        ProductResponse.Create response = productFacade.createProduct(request, principal);

        // then
        assertThat(response).isNotNull();
        verify(companyService).getCompany(companyId);
        verify(productService).createProduct(mockCompany, request, principal);
    }

    @Test
    @DisplayName("상품 목록 조회 테스트")
    void searchProductsTest() {
        // given
        String keyword = "테스트";
        Pageable pageable = PageRequest.of(0, 10);
        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);

        Company mockCompany = createCompany(companyId, hubId);
        Product mockProduct = createProduct(productId, "테스트 상품", mockCompany, 1500L, 10);
        Page<Product> mockPage = new PageImpl<>(List.of(mockProduct), pageable, 1);
        HubResponse mockHub = new HubResponse(hubId, "테스트 허브");
        ApiResponse<List<HubResponse>> mockHubFeign = ApiResponse.success(List.of(mockHub));

        given(productService.searchProducts(keyword, true, companyId, hubId, pageable, principal)).willReturn(mockPage);
        given(hubClient.getHubs(Set.of(hubId))).willReturn(mockHubFeign);

        // when
        PageResponse<ProductResponse.Summary> response = productFacade.searchProducts(keyword, true, companyId, hubId, pageable, principal);

        // then
        assertThat(response.getContent()).hasSize(1);
        verify(productService).searchProducts(keyword, true, companyId, hubId, pageable, principal);
        verify(hubClient).getHubs(Set.of(hubId));
    }

    @Nested
    @DisplayName("상품 조회 테스트")
    class GetProductTest {
        private final UUID createUserId = UUID.randomUUID();
        private final UUID updateUserId = UUID.randomUUID();

        private final CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        private final Company mockCompany = createCompany(companyId, hubId);

        @Test
        @DisplayName("성공: 숨김 상품 아님")
        void test1() {
            // given
            Product mockProduct = createProduct(productId, "테스트 상품", mockCompany, 2000L, 5);
            ReflectionTestUtils.setField(mockProduct, "status", ProductStatus.HIDDEN);
            ReflectionTestUtils.setField(mockProduct, "createdBy", createUserId);
            ReflectionTestUtils.setField(mockProduct, "updatedBy", updateUserId);

            HubResponse mockHub = new HubResponse(hubId, "테스트 허브");
            ApiResponse<HubResponse> mockHubFeign = ApiResponse.success(mockHub);

            UserResponse user1 = new UserResponse(createUserId, "생성자");
            UserResponse user2 = new UserResponse(updateUserId, "수정자");
            ApiResponse<List<UserResponse>> mockUserFeign = ApiResponse.success(List.of(user1, user2));

            given(productService.getProduct(productId)).willReturn(mockProduct);
            given(principal.isAccessibleHub(hubId)).willReturn(true);
            given(principal.isAccessibleCompany(companyId)).willReturn(true);
            given(hubClient.getHub(hubId)).willReturn(mockHubFeign);
            given(userClient.getUsers(Set.of(createUserId, updateUserId))).willReturn(mockUserFeign);

            // when
            ProductResponse.Detail response = productFacade.getProduct(productId, principal);

            // then
            assertThat(response).isNotNull();
            verify(hubClient).getHub(hubId);
            verify(userClient).getUsers(Set.of(createUserId, updateUserId));
        }

        @Test
        @DisplayName("실패: 숨김 상품 접근 권한 없음")
        void test2() {
            // given
            Product mockProduct = createProduct(productId, "테스트 상품(숨김)", mockCompany, 2000L, 5);
            ReflectionTestUtils.setField(mockProduct, "status", ProductStatus.HIDDEN);

            given(productService.getProduct(productId)).willReturn(mockProduct);
            given(principal.isAccessibleHub(hubId)).willReturn(false);
            given(principal.isAccessibleCompany(companyId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> productFacade.getProduct(productId, principal))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }
    }

    @Test
    @DisplayName("상품 수정 테스트")
    void updateProductTest() {
        // given
        ProductRequest.Update request = new ProductRequest.Update("테스트 상품", "설명", 10L);
        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        Company mockCompany = createCompany(companyId, hubId);
        Product mockProduct = createProduct(productId, "테스트 상품(수정)", mockCompany, 10L, 10);

        given(productService.updateProduct(productId, request, principal)).willReturn(mockProduct);

        // when
        productFacade.updateProduct(productId, request, principal);

        // then
        verify(productService).updateProduct(productId, request, principal);
    }

    @Nested
    @DisplayName("상품 재고 변경 테스트")
    class UpdateProductQuantityTest {
        private final ProductRequest.StockUpdate request = new ProductRequest.StockUpdate(-5);
        private final CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        private final Company mockCompany = createCompany(companyId, hubId);
        private final Product mockProduct = createProduct(productId, "테스트 상품", mockCompany, 100L, 10);

        @Test
        @DisplayName("성공: 상품 재고 변경 권한 있음")
        void test1() {
            // given
            given(productService.getProduct(productId)).willReturn(mockProduct);
            given(productService.updateProductQuantity(productId, request.getUpdateQuantity())).willReturn(mockProduct);
            given(principal.isAccessibleHub(hubId)).willReturn(true);
            given(principal.isAccessibleCompany(companyId)).willReturn(true);

            // when
            ProductResponse.StockUpdate response = productFacade.updateProductQuantity(productId, request, principal);

            // then
            assertThat(response).isNotNull();
            verify(productService).getProduct(productId);
            verify(productService).updateProductQuantity(productId, request.getUpdateQuantity());
        }

        @Test
        @DisplayName("실패: 상품 재고 변경 권한 없음")
        void test2() {
            // given
            given(productService.getProduct(productId)).willReturn(mockProduct);
            given(principal.isAccessibleHub(hubId)).willReturn(false);
            given(principal.isAccessibleCompany(companyId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> productFacade.updateProductQuantity(productId, request, principal))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(ProductErrorCode.PRODUCT_UPDATE_DENIED.getMessage());
        }
    }

    @Nested
    @DisplayName("주문 상품 재고 변경 테스트")
    class UpdateProductQuantityForOrderTest {
        private final UUID orderId = UUID.randomUUID();
        private final String redisKey = "rollback:order:" + orderId;

        @Test
        @DisplayName("성공: 이미 처리 중이거나 완료된 요청")
        void test1() {
            // given
            ProductRequest.StockItem stockItem = new ProductRequest.StockItem(productId, 5);
            ProductRequest.OrderStockUpdate requests = new ProductRequest.OrderStockUpdate(orderId, List.of(stockItem));

            Company mockCompany = createCompany(companyId, hubId);
            Product mockProduct = createProduct(productId, "테스트 상품", mockCompany, 10L, 10);

            given(productService.reserveRollbackHistory(redisKey)).willReturn(false);
            given(productService.findAllByIds(anyList())).willReturn(List.of(mockProduct));

            // when
            List<ProductResponse.StockUpdate> responses = productFacade.updateProductQuantityForOrder(requests);

            // then
            assertThat(responses).hasSize(1);
            verify(productService).reserveRollbackHistory(redisKey);
            verify(productService).findAllByIds(anyList());
            verify(productService, never()).updateProductQuantityForOrder(anyList(), anyMap(), anyBoolean());
            verify(productService, never()).confirmRollbackHistory(anyString());
            verify(productService, never()).clearRollbackHistory(anyString());
        }

        @Test
        @DisplayName("성공: 새로운 요청")
        void test2() {
            // given
            UUID productId2 = UUID.randomUUID();

            ProductRequest.StockItem stockItem1 = new ProductRequest.StockItem(productId2, 2);
            ProductRequest.StockItem stockItem2 = new ProductRequest.StockItem(productId, 3);
            ProductRequest.OrderStockUpdate requests = new ProductRequest.OrderStockUpdate(orderId, List.of(stockItem1, stockItem2));

            Company mockCompany = createCompany(companyId, hubId);
            Product product1 = createProduct(productId, "테스트 상품1", mockCompany, 10L, 10);
            Product product2 = createProduct(productId2, "테스트 상품2", mockCompany, 20L, 20);

            List<UUID> sortedIds = Stream.of(productId2, productId).sorted().toList();

            given(productService.reserveRollbackHistory(redisKey)).willReturn(true);
            given(productService.updateProductQuantityForOrder(eq(sortedIds), anyMap(), anyBoolean())).willReturn(List.of(product1, product2));

            // when
            productFacade.updateProductQuantityForOrder(requests);

            // then
            verify(productService).reserveRollbackHistory(redisKey);
            verify(productService).updateProductQuantityForOrder(eq(sortedIds), anyMap(), anyBoolean());
            verify(productService).confirmRollbackHistory(redisKey);
            verify(productService, never()).clearRollbackHistory(anyString());
        }

        @Test
        @DisplayName("실패: 최초 요청 중 예외 발생")
        void test3() {
            // given
            ProductRequest.StockItem stockItem = new ProductRequest.StockItem(productId, 5);
            ProductRequest.OrderStockUpdate requests = new ProductRequest.OrderStockUpdate(orderId, List.of(stockItem));

            given(productService.reserveRollbackHistory(redisKey)).willReturn(true);
            given(productService.updateProductQuantityForOrder(anyList(), anyMap(), anyBoolean()))
                    .willThrow(new BaseException(ProductErrorCode.OUT_OF_STOCK));

            // when & then
            assertThatThrownBy(() -> productFacade.updateProductQuantityForOrder(requests))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(ProductErrorCode.OUT_OF_STOCK.getMessage());

            verify(productService).reserveRollbackHistory(redisKey);
            verify(productService).clearRollbackHistory(redisKey);
            verify(productService, never()).confirmRollbackHistory(anyString());
        }

        @Test
        @DisplayName("실패: 일관성 없는 요청")
        void test4() {
            // given
            ProductRequest.StockItem stockItem1 = new ProductRequest.StockItem(productId, 5);
            ProductRequest.StockItem stockItem2 = new ProductRequest.StockItem(productId, -5);
            ProductRequest.OrderStockUpdate requests = new ProductRequest.OrderStockUpdate(orderId, List.of(stockItem1, stockItem2));

            // when & then
            assertThatThrownBy(() -> productFacade.updateProductQuantityForOrder(requests))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CommonErrorCode.INVALID_REQUEST.getMessage());

            verifyNoInteractions(productService);
        }
    }

    @Test
    @DisplayName("상품 삭제 테스트")
    void deleteProductTest() {
        // given
        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        Company mockCompany = createCompany(companyId, hubId);
        Product mockProduct = createProduct(productId, "테스트 상품(삭제)", mockCompany, 10L, 5);

        given(productService.deleteProduct(productId, principal)).willReturn(mockProduct);

        // when
        productFacade.deleteProduct(productId, principal);

        // then
        verify(productService).deleteProduct(productId, principal);
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
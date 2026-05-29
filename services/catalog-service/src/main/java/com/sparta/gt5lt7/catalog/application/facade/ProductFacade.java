package com.sparta.gt5lt7.catalog.application.facade;

import com.sparta.gt5lt7.common.exception.CommonErrorCode;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.catalog.global.exception.ProductErrorCode;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.UserClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import com.sparta.gt5lt7.common.dto.PageResponse;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.application.service.CompanyService;
import com.sparta.gt5lt7.catalog.application.service.ProductService;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ProductFacade {
    private final ProductService productService;
    private final CompanyService companyService;
    private final HubClient hubClient;
    private final UserClient userClient;

    private static final String REDIS_ROLLBACK_KEY_PREFIX = "rollback:order:";

    public ProductResponse.Create createProduct(ProductRequest.Create request, CustomUserPrincipal principal) {
        Company company = companyService.getCompany(request.getCompanyId());
        Product product = productService.createProduct(company, request, principal);
        return ProductResponse.Create.from(product);
    }

    public PageResponse<ProductResponse.Summary> searchProducts(
            String keyword, Boolean salesOnly, UUID companyId, UUID hubId,
            Pageable pageable, CustomUserPrincipal principal
    ) {
        // [서비스 레이어] 상품 목록 조회
        Page<Product> productPage = productService.searchProducts(keyword, salesOnly, companyId, hubId, pageable, principal);

        // [MSA 통신] 허브 ID를 중복 없이 추출 → Hub Service로 허브 정보 요청
        Set<UUID> hubIds = productPage.stream().map(product -> product.getCompany().getHubId()).collect(Collectors.toSet());
        List<HubResponse> hubs = hubIds.isEmpty() ? List.of() : hubClient.getHubs(hubIds).getData();

        // O(1) 조회를 위한 허브 Map 생성
        Map<UUID, HubResponse> hubMap = hubs.stream().collect(Collectors.toMap(HubResponse::id, Function.identity()));

        return PageResponse.of(productPage, product -> {
            HubResponse hub = hubMap.get(product.getCompany().getHubId());
            return ProductResponse.Summary.of(product, hub);
        });
    }

    public ProductResponse.Detail getProduct(UUID id, CustomUserPrincipal principal) {
        // [서비스 레이어] 상품 조회
        Product product = productService.getProduct(id);
        Company company = product.getCompany();

        // [권한 검증] 숨김 상품일 때 권한 처리
        if (product.isHidden()) {
            if (principal == null ||
                    (!principal.isAccessibleHub(company.getHubId()) && !principal.isAccessibleCompany(company.getCompanyId()))) {
                throw new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND);
            }
        }

        // [MSA 통신] Hub Service로 허브 정보 요청
        HubResponse hub = hubClient.getHub(company.getHubId()).getData();

        // [MSA 통신] 사용자 ID를 중복 없이 추출 → User Service로 사용자 정보 요청
        Set<UUID> userIds = Stream.of(product.getCreatedBy(), product.getUpdatedBy())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        List<UserResponse> users = userClient.getUsers(userIds).getData();

        // O(1) 조회를 위한 사용자 Map 생성
        Map<UUID, UserResponse> userMap = users.stream().collect(Collectors.toMap(UserResponse::id, user -> user));

        // 생성자/수정자 정보 처리
        UserResponse createdBy = UserResponse.from(product.getCreatedBy(), userMap);
        UserResponse updatedBy = UserResponse.from(product.getUpdatedBy(), userMap);

        return ProductResponse.Detail.of(product, hub, createdBy, updatedBy);
    }

    public ProductResponse.Update updateProduct(UUID id, ProductRequest.Update request, CustomUserPrincipal principal) {
        Product product = productService.updateProduct(id, request, principal);
        return ProductResponse.Update.from(product);
    }

    public ProductResponse.StatusUpdate updateProductStatus(UUID id, ProductRequest.StatusUpdate request, CustomUserPrincipal principal) {
        Product product = productService.updateProductStatus(id, request.getAction(), principal);
        return ProductResponse.StatusUpdate.from(product);
    }

    public ProductResponse.StockUpdate updateProductQuantity(UUID id, ProductRequest.StockUpdate request, CustomUserPrincipal principal) {
        Product product = productService.getProduct(id);
        Company company = product.getCompany();

        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!principal.isAccessibleHub(company.getHubId()) && !principal.isAccessibleCompany(company.getCompanyId())) {
            throw new BaseException(ProductErrorCode.PRODUCT_UPDATE_DENIED);
        }

        product = productService.updateProductQuantity(id, request.getUpdateQuantity());
        return ProductResponse.StockUpdate.from(product);
    }

    public List<ProductResponse.StockUpdate> updateProductQuantityForOrder(ProductRequest.OrderStockUpdate requests) {
        List<ProductRequest.StockItem> stockItems = requests.getStockItems();

        if (stockItems.isEmpty()) {
            return Collections.emptyList();
        }

        // [일관성 검증] 모든 요청이 재고 차감이거나 원복인지 확인
        boolean isAllNegative = stockItems.stream().allMatch(item -> item.getUpdateQuantity() < 0);
        boolean isAllPositive = stockItems.stream().allMatch(item -> item.getUpdateQuantity() > 0);

        if (!isAllNegative && !isAllPositive) {
            throw new BaseException(CommonErrorCode.INVALID_REQUEST);
        }

        // [Redis 활용] 롤백 요청일 때, 보상 트랜잭션 중복 검증
        boolean isRollbackProcess = isAllPositive;
        String redisKey = REDIS_ROLLBACK_KEY_PREFIX + requests.getOrderId();

        // 1. 주문 ID로 롤백되었는지 확인
        if (isRollbackProcess) {
            // 최초 요청 시 임시 선점
            boolean isFirstRequest = productService.reserveRollbackHistory(redisKey);

            // 이미 처리 중이거나 완료된 요청 ⇾ 단순 조회 후 반환
            if (!isFirstRequest) {
                List<Product> products = productService.findAllByIds(stockItems);
                return products.stream().map(ProductResponse.StockUpdate::from).toList();
            }
        }

        // O(1) 조회를 위한 요청 Map 생성 → 중복되는 상품 ID의 변경 재고량을 병합해 안정성 확보
        Map<UUID, Integer> quantityMap = stockItems.stream()
                .collect(Collectors.toMap(
                        ProductRequest.StockItem::getProductId,
                        ProductRequest.StockItem::getUpdateQuantity,
                        Integer::sum
                ));

        // [데드락 방지] 상품 ID 오름차순 정렬 → 트랜잭션들이 항상 같은 순서로 락 점유
        List<UUID> productIds = quantityMap.keySet().stream().sorted().toList();

        try {
            // 정렬된 순서대로 DB에서 비관적 락을 걸고 데이터 조회
            List<Product> products = productService.updateProductQuantityForOrder(productIds, quantityMap);

            // 2-1. 롤백 처리 성공 시, Redis에 주문 ID 저장 → TTL 만료 시간 분산 적용
            if (isRollbackProcess) {
                productService.confirmRollbackHistory(redisKey);
            }

            return products.stream().map(ProductResponse.StockUpdate::from).toList();
        } catch (Exception e) {
            // 2-2. 예외 발생 시 Redis 키 삭제
            if (isRollbackProcess) {
                productService.clearRollbackHistory(redisKey);
            }
            throw e;
        }
    }

    public ProductResponse.Delete deleteProduct(UUID id, CustomUserPrincipal principal) {
        Product product = productService.deleteProduct(id, principal);
        return ProductResponse.Delete.from(product);
    }
}
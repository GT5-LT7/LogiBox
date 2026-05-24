package com.sparta.gt5lt7.catalog.application;

import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ActionType;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.domain.entity.Category;
import com.sparta.gt5lt7.catalog.global.exception.ProductErrorCode;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.UserClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.common.dto.PageResponse;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.repository.ProductRepository;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final HubClient hubClient;
    private final UserClient userClient;
    private final CompanyService companyService;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_ROLLBACK_KEY_PREFIX = "rollback:order:";

    private static final long BASE_TIMEOUT_SECONDS = 600L; // 기본 10분
    private static final long RANDOM_BUFFER_MAX_SECONDS = 180L; // 최대 3분 랜덤 버퍼

    @Transactional
    public ProductResponse.Create createProduct(ProductRequest.Create request, CustomUserPrincipal principal) {
        Company company = companyService.getCompanyById(request.getCompanyId());

        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!principal.isAccessibleHub(company.getHubId()) && !principal.isAccessibleCompany(request.getCompanyId())) {
            throw new BaseException(ProductErrorCode.PRODUCT_CREATE_DENIED);
        }

        Category category = categoryService.getCategoryById(request.getCategoryId());
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .company(company)
                .category(category)
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();

        Product savedProduct = productRepository.save(product);
        return ProductResponse.Create.from(savedProduct);
    }

    public PageResponse<ProductResponse.Summary> searchProducts(
            String keyword, UUID companyId, UUID hubId, UUID categoryId, Pageable pageable
    ) {
        Page<Product> productPage = productRepository.searchProducts(keyword, companyId, hubId, categoryId, pageable);

        // 허브 ID를 중복 없이 추출 → Hub Service로 허브 정보 요청
        Set<UUID> hubIds = productPage.stream()
                .map(product -> product.getCompany().getHubId())
                .collect(Collectors.toSet());
        List<HubResponse> hubResponses = hubIds.isEmpty() ? List.of() : hubClient.getHubs(hubIds);

        // O(1) 조회를 위한 허브 Map 생성
        Map<UUID, HubResponse> hubMap = hubResponses.stream()
                .collect(Collectors.toMap(HubResponse::id, Function.identity()));

        return PageResponse.of(productPage, product -> {
            HubResponse hubResponse = hubMap.get(product.getCompany().getHubId());
            return ProductResponse.Summary.of(product, hubResponse);
        });
    }

    public ProductResponse.Detail getProduct(UUID id, CustomUserPrincipal principal) {
        Product product = getProductById(id);

        // 숨김 상품일 때 권한 처리
        if (product.isHidden()) {
            if (!principal.isAccessibleHub(product.getCompany().getHubId())
                    && !principal.isAccessibleCompany(product.getCompany().getCompanyId())) {
                throw new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND);
            }
        }

        // Hub Service로 허브 정보 요청
        HubResponse hubResponse = hubClient.getHub(product.getCompany().getHubId());

        // 사용자 ID를 중복 없이 추출 → User Service로 사용자 정보 요청
        Set<UUID> userIds = Set.of(product.getCreatedBy(), product.getUpdatedBy());
        List<UserResponse> userResponses = userClient.getUsers(userIds);

        // O(1) 조회를 위한 사용자 Map 생성
        Map<UUID, UserResponse> userMap = userResponses.stream()
                .collect(Collectors.toMap(UserResponse::id, user -> user));

        return ProductResponse.Detail.of(
                product, hubResponse, userMap.get(product.getCreatedBy()), userMap.get(product.getUpdatedBy())
        );
    }

    @Transactional
    public ProductResponse.Update updateProduct(UUID id, ProductRequest.Update request, CustomUserPrincipal principal) {
        Product product = getProductById(id);

        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!principal.isAccessibleHub(product.getCompany().getHubId())
                && !principal.isAccessibleCompany(product.getCompany().getCompanyId())) {
            throw new BaseException(ProductErrorCode.PRODUCT_UPDATE_DENIED);
        }

        // 변경된 카테고리 처리
        Category category = product.getCategory();
        if (!category.getCategoryId().equals(request.getCategoryId())) {
             category = categoryService.getCategoryById(request.getCategoryId());
        }

        product.update(request, category);

        return ProductResponse.Update.from(product);
    }

    @Transactional
    public ProductResponse.StatusUpdate updateProductStatus(UUID id, ProductRequest.StatusUpdate request, CustomUserPrincipal principal) {
        ActionType action = request.getAction();

        // Master가 아니면 STOP 액션 불가
        if (action == ActionType.STOP && !principal.isMaster()) {
            throw new BaseException(ProductErrorCode.PRODUCT_UPDATE_DENIED);
        }

        Product product = getProductById(id);

        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!principal.isAccessibleHub(product.getCompany().getHubId())
                && !principal.isAccessibleCompany(product.getCompany().getCompanyId())) {
            throw new BaseException(ProductErrorCode.PRODUCT_UPDATE_DENIED);
        }

        product.updateStatus(action);

        return ProductResponse.StatusUpdate.from(product);
    }

    @Transactional
    public ProductResponse.StockUpdate updateProductQuantity(UUID id, ProductRequest.StockUpdate request, CustomUserPrincipal principal) {
        // [데드락 방지] DB에서 비관적 락을 걸고 데이터 조회
        Product product = productRepository.findByIdInForUpdate(id);

        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!principal.isAccessibleHub(product.getCompany().getHubId())
                && !principal.isAccessibleCompany(product.getCompany().getCompanyId())) {
            throw new BaseException(ProductErrorCode.PRODUCT_UPDATE_DENIED);
        }

        // 재고 차감 및 원복
        int quantity = product.getQuantity() + request.getUpdateQuantity();

        // 재고 부족 예외 처리
        if (quantity < 0) {
            throw new BaseException(ProductErrorCode.OUT_OF_STOCK);
        }

        // 재고 변경
        product.updateQuantity(quantity);

        return ProductResponse.StockUpdate.from(product);
    }

    @Transactional
    public List<ProductResponse.StockUpdate> updateProductQuantityForOrder(ProductRequest.OrderStockUpdate requests) {
        List<ProductRequest.StockItem> stockItems = requests.getStockItems();

        if (stockItems.isEmpty()) {
            return Collections.emptyList();
        }

        // [Redis 활용] 원복 요청일 때, 보상 트랜잭션 중복 검증
        boolean isRollbackProcess = stockItems.stream().allMatch(item -> item.getUpdateQuantity() > 0);
        String redisKey = REDIS_ROLLBACK_KEY_PREFIX + requests.getOrderId();

        // 1. 주문 ID로 롤백되었는지 확인
        if (isRollbackProcess) {
            Boolean isAlreadyProcessed = redisTemplate.hasKey(redisKey);

            if (Objects.equals(isAlreadyProcessed, true)) {
                List<UUID> productIdsForRead = stockItems.stream()
                        .map(ProductRequest.StockItem::getProductId)
                        .collect(Collectors.toList());

                return productRepository.findAllById(productIdsForRead).stream()
                        .map(ProductResponse.StockUpdate::from)
                        .collect(Collectors.toList());
            }
        }

        // [데드락 방지] 상품 ID 목록을 추출한 뒤 오름차순 정렬 → 트랜잭션들이 항상 같은 순서로 락을 점유해 교착 상태 차단
        List<UUID> productIds = stockItems.stream()
                .map(ProductRequest.StockItem::getProductId)
                .sorted()
                .collect(Collectors.toList());

        // 정렬된 ID 순서대로 DB에서 비관적 락을 걸고 데이터 조회
        List<Product> products = productRepository.findAllByIdInForUpdate(productIds);

        // O(1) 조회를 위한 요청 Map 생성 → 상품 ID가 중복될 때, 변경 재고량을 병합해 안정성 확보
        Map<UUID, ProductRequest.StockItem> requestMap = stockItems.stream()
                .collect(Collectors.toMap(
                        ProductRequest.StockItem::getProductId,
                        item -> item,
                        (existing, replacement) -> ProductRequest.StockItem.builder()
                                .productId(existing.getProductId())
                                .updateQuantity(existing.getUpdateQuantity() + replacement.getUpdateQuantity())
                                .build()
                ));

        List<ProductResponse.StockUpdate> responses = new ArrayList<>();

        // 재고 차감 및 원복
        for (Product product : products) {
            ProductRequest.StockItem stockItem = requestMap.get(product.getProductId());
            int quantity = product.getQuantity() + stockItem.getUpdateQuantity();

            // 재고 부족 예외 처리
            if (quantity < 0) {
                throw new BaseException(ProductErrorCode.OUT_OF_STOCK);
            }

            // 재고 변경
            product.updateQuantity(quantity);

            responses.add(ProductResponse.StockUpdate.from(product));
        }

        // 2. 롤백 처리 성공 시, Redis에 주문 ID 저장 → TTL 만료 시간 분산 적용
        if (isRollbackProcess) {
            long randomBufferSeconds = ThreadLocalRandom.current().nextLong(RANDOM_BUFFER_MAX_SECONDS + 1);
            long totalTimeoutSeconds = BASE_TIMEOUT_SECONDS + randomBufferSeconds;

            redisTemplate.opsForValue().set(redisKey, "processed", totalTimeoutSeconds, TimeUnit.SECONDS);
        }

        return responses;
    }

    public ProductResponse.Delete deleteProduct(UUID id, CustomUserPrincipal principal) {
        Product product = getProductById(id);

        // Master가 아니면 담당 허브인지 검증
        if (!principal.isAccessibleHub(product.getCompany().getHubId())) {
            throw new BaseException(ProductErrorCode.PRODUCT_DELETE_DENIED);
        }

        // Soft Delete 처리
        product.softDelete(principal.userId());

        return ProductResponse.Delete.from(product);
    }

    // 업체 ID 기반 연쇄 삭제 메서드
    @Transactional
    public void deleteProducts(UUID companyId, UUID deletedBy) {
        productRepository.softDeleteByProductId(companyId, deletedBy, LocalDateTime.now());
    }

    // 상품 조회 공통 메서드
    public Product getProductById(UUID id) {
        return productRepository.findByIdWithCompanyAndCategory(id)
                .orElseThrow(() -> new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
}
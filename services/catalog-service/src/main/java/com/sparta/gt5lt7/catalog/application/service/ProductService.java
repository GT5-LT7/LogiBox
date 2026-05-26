package com.sparta.gt5lt7.catalog.application.service;

import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ActionType;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.global.exception.ProductErrorCode;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.repository.ProductRepository;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final long BASE_TIMEOUT_SECONDS = 600L; // 10분
    private static final long RANDOM_BUFFER_MAX_SECONDS = 60L; // 1분

    @Transactional
    public Product createProduct(Company company, ProductRequest.Create request, CustomUserPrincipal principal) {
        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!principal.isAccessibleHub(company.getHubId()) && !principal.isAccessibleCompany(request.getCompanyId())) {
            throw new BaseException(ProductErrorCode.PRODUCT_CREATE_DENIED);
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .company(company)
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();
        return productRepository.save(product);
    }

    public Page<Product> searchProducts(
            String keyword, Boolean salesOnly, UUID companyId, UUID hubId, Pageable pageable, CustomUserPrincipal principal
    ) {
        return productRepository.searchProducts(keyword, salesOnly, companyId, hubId, pageable, principal);
    }

    public Product getProduct(UUID id) {
        return productRepository.findByIdWithCompany(id).orElseThrow(() -> new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Transactional
    public Product updateProduct(UUID id, ProductRequest.Update request, CustomUserPrincipal principal) {
        Product product = getProduct(id);
        Company company = product.getCompany();

        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!principal.isAccessibleHub(company.getHubId()) && !principal.isAccessibleCompany(company.getCompanyId())) {
            throw new BaseException(ProductErrorCode.PRODUCT_UPDATE_DENIED);
        }

        product.update(request);
        return product;
    }

    @Transactional
    public Product updateProductStatus(UUID id, ActionType action, CustomUserPrincipal principal) {
        // Master가 아니면 STOP 액션 불가
        if (action == ActionType.STOP && !principal.isMaster()) {
            throw new BaseException(ProductErrorCode.PRODUCT_UPDATE_DENIED);
        }

        Product product = getProduct(id);
        Company company = product.getCompany();

        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!principal.isAccessibleHub(company.getHubId()) && !principal.isAccessibleCompany(company.getCompanyId())) {
            throw new BaseException(ProductErrorCode.PRODUCT_UPDATE_DENIED);
        }

        product.updateStatus(action);
        return product;
    }

    @Transactional
    public Product updateProductQuantity(UUID id, Integer updateQuantity) {
        // 비관적 락을 걸고 데이터 조회
        Product product = productRepository.findByIdInForUpdate(id);

        int quantity = product.getQuantity() + updateQuantity;
        if (quantity < 0) {
            throw new BaseException(ProductErrorCode.OUT_OF_STOCK);
        }

        product.updateQuantity(quantity);
        return product;
    }

    public List<ProductResponse.StockUpdate> findAllByIds(List<ProductRequest.StockItem> stockItems) {
        List<UUID> productIds = stockItems.stream().map(ProductRequest.StockItem::getProductId).collect(Collectors.toList());
        return productRepository.findAllById(productIds).stream().map(ProductResponse.StockUpdate::from).collect(Collectors.toList());
    }

    @Transactional
    public List<Product> updateProductQuantityForOrder(List<UUID> productIds, Map<UUID, Integer> quantityMap) {
        List<Product> products = productRepository.findAllByIdInForUpdate(productIds);

        // 재고 차감 및 원복
        for (Product product : products) {
            int quantity = product.getQuantity() + quantityMap.get(product.getProductId());

            if (quantity < 0) {
                throw new BaseException(ProductErrorCode.OUT_OF_STOCK);
            }

            product.updateQuantity(quantity);
        }

        return products;
    }

    public void saveRollbackHistoryToRedis(String redisKey) {
        long randomBufferSeconds = ThreadLocalRandom.current().nextLong(RANDOM_BUFFER_MAX_SECONDS + 1);
        long totalTimeoutSeconds = BASE_TIMEOUT_SECONDS + randomBufferSeconds;

        redisTemplate.opsForValue().set(redisKey, "processed", totalTimeoutSeconds, TimeUnit.SECONDS);
    }

    @Transactional
    public Product deleteProduct(UUID id, CustomUserPrincipal principal) {
        Product product = getProduct(id);

        // Master가 아니면 담당 허브인지 검증
        if (!principal.isAccessibleHub(product.getCompany().getHubId())) {
            throw new BaseException(ProductErrorCode.PRODUCT_DELETE_DENIED);
        }

        // Soft Delete 처리
        product.softDelete(principal.userId());

        return product;
    }

    // 업체 ID 기반 연쇄 삭제 메서드
    @Transactional
    public void deleteProducts(UUID companyId, UUID deletedBy) {
        productRepository.softDeleteByProductId(companyId, deletedBy, LocalDateTime.now());
    }
}
package com.sparta.gt5lt7.catalog.application;

import com.sparta.gt5lt7.catalog.domain.entity.Category;
import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.global.exception.ProductErrorCode;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.common.dto.PageResponse;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.repository.ProductRepository;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final HubClient hubClient;
    private final CompanyService companyService;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse.Create createProduct(ProductRequest.Create request, UUID hubOrCompanyId, List<String> roles) {
        Company company = companyService.getCompanyById(request.getCompanyId());

        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!roles.contains("ROLE_MASTER")) {
            // TODO: CustomUserDetails 구현이 완료되면 허브 또는 업체 ID를 기반으로 검증 로직 수정
            if (!company.getHubId().equals(hubOrCompanyId)) {
                throw new BaseException(ProductErrorCode.PRODUCT_CREATE_DENIED);
            }
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
        List<HubResponse> hubResponses = hubClient.getHubs(hubIds);

        // O(1) 조회를 위한 허브 Map 생성
        Map<UUID, HubResponse> hubMap = hubResponses.stream()
                .collect(Collectors.toMap(HubResponse::id, Function.identity()));

        return PageResponse.of(productPage, product -> {
            HubResponse hubResponse = hubMap.get(product.getCompany().getHubId());
            return ProductResponse.Summary.of(product, hubResponse);
        });
    }

    @Transactional
    public ProductResponse.Update updateProduct(UUID id, ProductRequest.Update request, UUID hubOrCompanyId, List<String> roles) {
        Product product = getProductById(id);

        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!roles.contains("ROLE_MASTER")) {
            // TODO: CustomUserDetails 구현이 완료되면 허브 또는 업체 ID를 기반으로 검증 로직 수정
            if (!product.getCompany().getHubId().equals(hubOrCompanyId)) {
                throw new BaseException(ProductErrorCode.PRODUCT_UPDATE_DENIED);
            }
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
    public ProductResponse.Delete deleteProduct(UUID id, UUID userAndHubId, List<String> roles) {
        Product product = getProductById(id);

        // Master가 아니면 담당 허브인지 검증
        if (!roles.contains("ROLE_MASTER")) {
            // TODO: CustomUserDetails 구현이 완료되면 허브 ID를 기반으로 검증 로직 수정
            if (!product.getCompany().getHubId().equals(userAndHubId)) {
                throw new BaseException(ProductErrorCode.PRODUCT_DELETE_DENIED);
            }
        }

        // Soft Delete 처리
        product.softDelete(userAndHubId);

        return ProductResponse.Delete.from(product);
    }

    // 업체 ID 기반 연쇄 삭제 메서드
    @Transactional
    public void deleteProducts(UUID companyId, UUID deletedBy) {
        productRepository.softDeleteByProductId(companyId, deletedBy, LocalDateTime.now());
    }

    // 상품 조회 공통 메서드
    public Product getProductById(UUID id) {
        return productRepository.findByIdWithCompany(id)
                .orElseThrow(() -> new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
}
package com.sparta.gt5lt7.catalog.application;

import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.domain.entity.Category;
import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.global.exception.ProductErrorCode;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.UserClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyResponse;
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
    private final UserClient userClient;
    private final CompanyService companyService;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;

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

    public ProductResponse.Detail getProduct(UUID id) {
        Product product = getProductById(id);

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
        if (!principal.isAccessibleHub(product.getCompany().getHubId()) && !principal.isAccessibleCompany(product.getCompany().getCompanyId())) {
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
        return productRepository.findByIdWithCompany(id)
                .orElseThrow(() -> new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
}
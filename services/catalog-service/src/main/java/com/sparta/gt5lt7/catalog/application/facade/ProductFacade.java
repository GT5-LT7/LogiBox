package com.sparta.gt5lt7.catalog.application.facade;

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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductFacade {
    private final ProductService productService;
    private final CompanyService companyService;
    private final HubClient hubClient;
    private final UserClient userClient;

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
        Set<UUID> hubIds = productPage.stream()
                .map(product -> product.getCompany().getHubId())
                .collect(Collectors.toSet());
        List<HubResponse> hubs = hubIds.isEmpty() ? List.of() : hubClient.getHubs(hubIds);

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
        HubResponse hub = hubClient.getHub(company.getHubId());

        // [MSA 통신] 사용자 ID를 중복 없이 추출 → User Service로 사용자 정보 요청
        Set<UUID> userIds = Set.of(product.getCreatedBy(), product.getUpdatedBy());
        List<UserResponse> users = userClient.getUsers(userIds);

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
}
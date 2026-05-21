package com.sparta.gt5lt7.catalog.application;

import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.global.exception.ProductErrorCode;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.repository.ProductRepository;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final HubClient hubClient;
    private final CompanyService companyService;
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

        // TODO: CategoryService 구현이 완료되면 주석 해제 후 상품 생성에 사용
        // Category category = categoryService.getCategoryById(request.getCategoryId());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .company(company)
                .category(null)
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();

        Product savedProduct = productRepository.save(product);
        return ProductResponse.Create.from(savedProduct);
    }
}
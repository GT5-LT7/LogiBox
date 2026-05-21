package com.sparta.gt5lt7.catalog.application;

import com.sparta.gt5lt7.catalog.domain.entity.Category;
import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.global.exception.ProductErrorCode;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.repository.ProductRepository;
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

        // TODO: CategoryService.getCategoryById 구현이 완료되면 주석 해제 후 상품 생성에 사용
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

    @Transactional
    public ProductResponse.Update updateProduct(UUID id, ProductRequest.Update request, UUID hubOrCompanyId, List<String> roles) {
        Product product = getCompanyById(id);

        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!roles.contains("ROLE_MASTER")) {
            // TODO: CustomUserDetails 구현이 완료되면 허브 또는 업체 ID를 기반으로 검증 로직 수정
            if (!product.getCompany().getHubId().equals(hubOrCompanyId)) {
                throw new BaseException(ProductErrorCode.PRODUCT_UPDATE_DENIED);
            }
        }

        Category category = product.getCategory();
        if (category.getCategoryId() != request.getCategoryId()) {
            // TODO: CategoryService.getCategoryById 구현이 완료되면 주석 해제
            // category = categoryService.getCategoryById(request.getCategoryId());
        }

        product.update(request, category);

        return ProductResponse.Update.from(product);
    }

    // 상품 조회 공통 메서드
    public Product getCompanyById(UUID id) {
        return productRepository.findByIdWithCompany(id)
                .orElseThrow(() -> new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
}
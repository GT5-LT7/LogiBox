package com.sparta.gt5lt7.catalog.domain.repository;

import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.gt5lt7.catalog.domain.entity.*;
import com.sparta.gt5lt7.catalog.global.util.QueryDslUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchProducts(
            String keyword, Boolean salesOnly, UUID companyId, UUID hubId,
            Pageable pageable, CustomUserPrincipal principal
    ) {
        QProduct product = QProduct.product;
        QCompany company = QCompany.company; // 조인용 Q클래스

        BooleanBuilder builder = new BooleanBuilder();

        // 1. 검색 및 필터 조건
        if (keyword != null && !keyword.isBlank()) {
            builder.and(product.name.containsIgnoreCase(keyword));
        }
        if (Boolean.TRUE.equals(salesOnly)) {
            builder.and(product.status.eq(ProductStatus.ON_SALE));
        }
        if (companyId != null) {
            builder.and(company.companyId.eq(companyId));
        }
        if (hubId != null) {
            builder.and(company.hubId.eq(hubId));
        }

        // 2. 숨김 상품에 대한 권한 제어
        if (principal == null || !principal.isMaster()) {
            // 기본적으로 숨김 상품 제외
            BooleanBuilder hiddenFilter = new BooleanBuilder(product.status.ne(ProductStatus.HIDDEN));

            // 담당 허브 또는 본인 업체의 숨김 상품은 볼 수 있도록 동적 조건 추가
            if (principal != null) {
                if (principal.hubId() != null) {
                    hiddenFilter.or(product.status.eq(ProductStatus.HIDDEN).and(company.hubId.eq(principal.hubId())));
                } else if (principal.companyId() != null) {
                    hiddenFilter.or(product.status.eq(ProductStatus.HIDDEN).and(company.companyId.eq(principal.companyId())));
                }
            }

            builder.and(hiddenFilter);
        }

        // 3. 정렬 조건
        List<OrderSpecifier<?>> orderSpecifiers = QueryDslUtil.getOrderSpecifiers(pageable, product);

        // 4. 실제 데이터 조회
        List<Product> content = queryFactory
                .selectFrom(product)
                .join(product.company, company).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .fetch();

        // 5. total 조회
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .join(product.company, company)
                .where(builder)
                .fetchOne();
        long totalCount = (total != null) ? total : 0L;

        return PageableExecutionUtils.getPage(content, pageable, () -> totalCount);
    }
}
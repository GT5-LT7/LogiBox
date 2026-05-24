package com.sparta.gt5lt7.catalog.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.gt5lt7.catalog.domain.entity.Product;
import com.sparta.gt5lt7.catalog.domain.entity.QCategory;
import com.sparta.gt5lt7.catalog.domain.entity.QCompany;
import com.sparta.gt5lt7.catalog.domain.entity.QProduct;
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
    public Page<Product> searchProducts(String keyword, UUID companyId, UUID hubId, UUID categoryId, Pageable pageable) {
        QProduct product = QProduct.product;

        // 조인용 Q클래스
        QCompany company = QCompany.company;
        QCategory category = QCategory.category;

        BooleanBuilder builder = new BooleanBuilder();

        // 1. 검색 및 필터 조건
        if (keyword != null && !keyword.isBlank()) {
            builder.and(product.name.containsIgnoreCase(keyword));
        }
        if (companyId != null) {
            builder.and(company.companyId.eq(companyId));
        }
        if (hubId != null) {
            builder.and(company.hubId.eq(hubId));
        }
        if (categoryId != null) {
            builder.and(category.categoryId.eq(categoryId));
        }

        // 2. 정렬 조건
        List<OrderSpecifier<?>> orderSpecifiers = QueryDslUtil.getOrderSpecifiers(pageable, product);

        // 3. 실제 데이터 조회
        List<Product> content = queryFactory
                .selectFrom(product)
                .join(product.company, company).fetchJoin()
                .leftJoin(product.category, category).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .fetch();

        // 4. total 조회
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .join(product.company, company)
                .leftJoin(product.category, category)
                .where(builder)
                .fetchOne();
        long totalCount = (total != null) ? total : 0L;

        return PageableExecutionUtils.getPage(content, pageable, () -> totalCount);
    }
}
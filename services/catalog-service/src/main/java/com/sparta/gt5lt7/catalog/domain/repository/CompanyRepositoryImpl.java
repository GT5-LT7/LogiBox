package com.sparta.gt5lt7.catalog.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.domain.entity.QCompany;
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
public class CompanyRepositoryImpl implements CompanyRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Company> searchCompanies(String keyword, CompanyType type, UUID hubId, Pageable pageable) {
        QCompany company = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();

        // 1. 검색 및 필터 조건
        if (keyword != null && !keyword.isBlank()) {
            builder.and(company.name.containsIgnoreCase(keyword));
        }
        if (type != null) {
            builder.and(company.type.eq(type));
        }
        if (hubId != null) {
            builder.and(company.hubId.eq(hubId));
        }

        // 2. 정렬 조건
        List<OrderSpecifier<?>> orderSpecifiers = QueryDslUtil.getOrderSpecifiers(pageable, company);

        // 3. 실제 데이터 조회
        List<Company> content = queryFactory
                .selectFrom(company)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .fetch();

        // 4. total 조회
        Long total = queryFactory
                .select(company.count())
                .from(company)
                .where(builder)
                .fetchOne();
        long totalCount = (total != null) ? total : 0L;

        return PageableExecutionUtils.getPage(content, pageable, () -> totalCount);
    }
}
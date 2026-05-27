package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import com.sparta.gt5lt7.logisticsservice.domain.entity.QHub;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HubRepositoryImpl implements HubRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Hub> searchHubs(String keyword, Pageable pageable) {
        QHub hub = QHub.hub;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(hub.deletedAt.isNull());

        String trimmed = keyword == null ? null : keyword.trim();
        if (StringUtils.hasText(trimmed)) {
            builder.and(
                    hub.name.containsIgnoreCase(trimmed)
                            .or(hub.address.containsIgnoreCase(trimmed))
            );
        }

        OrderSpecifier<?> orderSpec = resolveOrder(pageable.getSort(), hub.createdAt, hub.updatedAt);

        List<Hub> content = queryFactory
                .selectFrom(hub)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpec)  // ← 기존 .orderBy(hub.createdAt.desc()) 교체
                .fetch();

        Long total = queryFactory
                .select(hub.count())
                .from(hub)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private OrderSpecifier<?> resolveOrder(
            Sort sort,
            DateTimePath<LocalDateTime> createdAt,
            DateTimePath<LocalDateTime> updatedAt
    ) {
        Sort.Order updated = sort.getOrderFor("updatedAt");
        if (updated != null) {
            return updated.isAscending() ? updatedAt.asc() : updatedAt.desc();
        }
        Sort.Order created = sort.getOrderFor("createdAt");
        if (created != null && created.isAscending()) {
            return createdAt.asc();
        }
        return createdAt.desc();
    }
}
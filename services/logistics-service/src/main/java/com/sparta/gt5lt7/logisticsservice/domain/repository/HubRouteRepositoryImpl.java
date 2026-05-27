package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.gt5lt7.logisticsservice.domain.entity.HubRoute;
import com.sparta.gt5lt7.logisticsservice.domain.entity.QHubRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HubRouteRepositoryImpl implements HubRouteRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<HubRoute> searchHubRoutes(UUID fromHubId, UUID toHubId, Pageable pageable) {
        QHubRoute route = QHubRoute.hubRoute;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(route.deletedAt.isNull());

        if (fromHubId != null) builder.and(route.fromHubId.eq(fromHubId));
        if (toHubId != null) builder.and(route.toHubId.eq(toHubId));

        OrderSpecifier<?> orderSpec = resolveOrder(pageable.getSort(), route.createdAt, route.updatedAt);

        List<HubRoute> content = queryFactory
                .selectFrom(route)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpec)
                .fetch();

        Long total = queryFactory
                .select(route.count())
                .from(route)
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
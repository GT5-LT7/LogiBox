package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.gt5lt7.logisticsservice.domain.entity.HubRoute;
import com.sparta.gt5lt7.logisticsservice.domain.entity.QHubRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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

        if (fromHubId != null) {
            builder.and(route.fromHubId.eq(fromHubId));
        }
        if (toHubId != null) {
            builder.and(route.toHubId.eq(toHubId));
        }

        List<HubRoute> content = queryFactory
                .selectFrom(route)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(route.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(route.count())
                .from(route)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
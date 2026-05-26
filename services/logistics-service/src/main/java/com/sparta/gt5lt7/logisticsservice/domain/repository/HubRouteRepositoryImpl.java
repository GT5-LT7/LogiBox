package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.gt5lt7.logisticsservice.domain.entity.QHubRoute;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRouteSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HubRouteRepositoryImpl implements HubRouteRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<HubRouteResponse> searchHubRoutes(HubRouteSearchRequest request, Pageable pageable) {
        QHubRoute route = QHubRoute.hubRoute;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(route.deletedAt.isNull());

        if (request.getFromHubId() != null) {
            builder.and(route.fromHubId.eq(request.getFromHubId()));
        }
        if (request.getToHubId() != null) {
            builder.and(route.toHubId.eq(request.getToHubId()));
        }

        List<HubRouteResponse> content = queryFactory
                .selectFrom(route)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(route.createdAt.desc())
                .fetch()
                .stream()
                .map(HubRouteResponse::from)
                .toList();

        Long total = queryFactory
                .select(route.count())
                .from(route)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
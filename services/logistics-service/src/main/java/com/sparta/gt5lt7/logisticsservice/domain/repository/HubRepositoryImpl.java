package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.gt5lt7.logisticsservice.domain.entity.QHub;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HubRepositoryImpl implements HubRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<HubResponse> searchHubs(HubSearchRequest request, Pageable pageable) {
        QHub hub = QHub.hub;
        BooleanBuilder builder = new BooleanBuilder();

        // 삭제된 데이터 제외
        builder.and(hub.deletedAt.isNull());

        // 이름/주소 키워드 검색 (OR 조건)
        if (StringUtils.hasText(request.getKeyword())) {
            builder.and(
                    hub.name.containsIgnoreCase(request.getKeyword())
                            .or(hub.address.containsIgnoreCase(request.getKeyword()))
            );
        }

        List<HubResponse> content = queryFactory
                .selectFrom(hub)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(hub.createdAt.desc())
                .fetch()
                .stream()
                .map(HubResponse::from)
                .toList();

        Long total = queryFactory
                .select(hub.count())
                .from(hub)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
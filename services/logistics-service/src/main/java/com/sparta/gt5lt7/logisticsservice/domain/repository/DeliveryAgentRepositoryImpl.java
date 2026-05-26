package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.gt5lt7.logisticsservice.domain.entity.QDeliveryAgent;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryAgentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DeliveryAgentRepositoryImpl implements DeliveryAgentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<DeliveryAgentResponse> searchDeliveryAgents(
            DeliveryAgentSearchRequest request, Pageable pageable
    ) {
        QDeliveryAgent agent = QDeliveryAgent.deliveryAgent;
        BooleanBuilder builder = new BooleanBuilder();

        // 삭제된 데이터 제외
        builder.and(agent.deletedAt.isNull());

        if (request.getHubId() != null) {
            builder.and(agent.hubId.eq(request.getHubId()));
        }
        if (request.getAgentType() != null) {
            builder.and(agent.agentType.eq(request.getAgentType()));
        }
        if (StringUtils.hasText(request.getSlackUserId())) {
            builder.and(agent.slackUserId.containsIgnoreCase(request.getSlackUserId().trim()));
        }

        List<DeliveryAgentResponse> content = queryFactory
                .selectFrom(agent)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(agent.deliverySequence.asc(), agent.createdAt.desc())
                .fetch()
                .stream()
                .map(DeliveryAgentResponse::from)
                .toList();

        Long total = queryFactory
                .select(agent.count())
                .from(agent)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
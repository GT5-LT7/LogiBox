package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryAgent;
import com.sparta.gt5lt7.logisticsservice.domain.entity.QDeliveryAgent;
import com.sparta.gt5lt7.logisticsservice.domain.entity.type.AgentType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DeliveryAgentRepositoryImpl implements DeliveryAgentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<DeliveryAgent> searchDeliveryAgents(
            UUID hubId,
            AgentType agentType,
            String slackUserId,
            Pageable pageable
    ) {
        QDeliveryAgent agent = QDeliveryAgent.deliveryAgent;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(agent.deletedAt.isNull());

        if (hubId != null) {
            builder.and(agent.hubId.eq(hubId));
        }
        if (agentType != null) {
            builder.and(agent.agentType.eq(agentType));
        }
        if (StringUtils.hasText(slackUserId)) {
            builder.and(agent.slackUserId.containsIgnoreCase(slackUserId.trim()));
        }

        List<DeliveryAgent> content = queryFactory
                .selectFrom(agent)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(agent.deliverySequence.asc(), agent.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(agent.count())
                .from(agent)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
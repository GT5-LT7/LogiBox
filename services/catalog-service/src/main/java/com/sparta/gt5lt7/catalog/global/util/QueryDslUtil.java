package com.sparta.gt5lt7.catalog.global.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class QueryDslUtil {
    private QueryDslUtil() {} // 객체 생성 방지

    // 정렬 조건 처리 메서드
    public static List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable, EntityPathBase<?> qClass) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        // Q클래스 정보로부터 메타데이터 추출
        PathBuilder<?> pathBuilder = new PathBuilder<>(qClass.getType(), qClass.getMetadata());

        for (Sort.Order order : pageable.getSort()) {
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();

            if ("createdAt".equals(property) || "updatedAt".equals(property)) {
                orders.add(new OrderSpecifier<>(direction, pathBuilder.getDateTime(property, LocalDateTime.class)));
            }
        }
        return orders;
    }
}
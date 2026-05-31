package com.sparta.gt5lt7.user.infrastructure.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.user.presentation.dto.request.UserSearchCondition;
import com.sparta.gt5lt7.user.domain.entity.QUser;
import com.sparta.gt5lt7.user.domain.entity.User;
import com.sparta.gt5lt7.user.domain.entity.UserStatus;
import com.sparta.gt5lt7.user.domain.repository.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QUser user = QUser.user;

    @Override
    public Page<User> searchUsers(UserSearchCondition condition, Pageable pageable) {

        List<User> content = queryFactory
                .selectFrom(user)
                .where(
                        usernameContains(condition.getUsername()),
                        roleEq(condition.getRole()),
                        statusEq(condition.getStatus()),
                        user.deletedAt.isNull()         // 소프트 삭제 제외
                )
                .orderBy(getOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        usernameContains(condition.getUsername()),
                        roleEq(condition.getRole()),
                        statusEq(condition.getStatus()),
                        user.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    // 검색 조건
    private BooleanExpression usernameContains(String username) {
        return username != null ? user.username.containsIgnoreCase(username) : null;
    }

    private BooleanExpression roleEq(UserRole role) {
        return role != null ? user.role.eq(role) : null;
    }

    private BooleanExpression statusEq(UserStatus status) {
        return status != null ? user.status.eq(status) : null;
    }

    // 정렬 조건 (생성일순, 수정일순)
    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return user.createdAt.desc();   // 기본 정렬: 생성일 내림차순
        }

        Sort.Order order = pageable.getSort().iterator().next();
        boolean isAsc = order.isAscending();

        return switch (order.getProperty()) {
            case "updatedAt" -> isAsc ? user.updatedAt.asc() : user.updatedAt.desc();
            default -> isAsc ? user.createdAt.asc() : user.createdAt.desc();
        };
    }
}
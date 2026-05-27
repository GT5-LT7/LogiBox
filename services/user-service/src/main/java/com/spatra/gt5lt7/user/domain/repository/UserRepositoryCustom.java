package com.spatra.gt5lt7.user.domain.repository;

import com.spatra.gt5lt7.user.application.dto.UserSearchCondition;
import com.spatra.gt5lt7.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> searchUsers(UserSearchCondition condition, Pageable pageable);
}
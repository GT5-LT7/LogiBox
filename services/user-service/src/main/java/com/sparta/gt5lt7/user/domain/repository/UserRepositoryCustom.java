package com.sparta.gt5lt7.user.domain.repository;

import com.sparta.gt5lt7.user.presentation.dto.request.UserSearchCondition;
import com.sparta.gt5lt7.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> searchUsers(UserSearchCondition condition, Pageable pageable);
}
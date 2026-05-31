package com.sparta.gt5lt7.user.domain.repository;

import com.sparta.gt5lt7.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // 소프트 삭제 안된 유저만 조회
    Optional<User> findByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
}

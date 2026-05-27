package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HubRepository extends JpaRepository<Hub, UUID>, HubRepositoryCustom {
    Optional<Hub> findByHubIdAndDeletedAtIsNull(UUID hubId);

    boolean existsByNameAndDeletedAtIsNull(String name);
}
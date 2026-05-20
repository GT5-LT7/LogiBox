package com.sparta.gt5lt7.catalog.domain.repository;

import com.sparta.gt5lt7.catalog.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID>, CompanyRepositoryCustom {
    boolean existsByHubId(UUID hubId);
}
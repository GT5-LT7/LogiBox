package com.sparta.gt5lt7.catalog.domain.repository;

import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomCompanyRepository {
    Page<Company> searchCompanies(String keyword, CompanyType type, UUID hubId, Pageable pageable);
}
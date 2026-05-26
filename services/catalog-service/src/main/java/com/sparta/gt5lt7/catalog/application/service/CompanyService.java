package com.sparta.gt5lt7.catalog.application.service;

import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.domain.repository.CompanyRepository;
import com.sparta.gt5lt7.catalog.global.exception.CompanyErrorCode;
import com.sparta.gt5lt7.catalog.presentation.dto.request.CompanyRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sparta.gt5lt7.common.exception.BaseException;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    @Transactional
    public Company createCompany(CompanyRequest request, CoordinateResponse coordinate) {
        Company company = Company.builder()
                .name(request.getName())
                .type(request.getType())
                .phone(request.getPhone())
                .hubId(request.getHubId())
                .baseAddress(request.getBaseAddress())
                .detailAddress(request.getDetailAddress())
                .zipcode(request.getZipcode())
                .latitude(coordinate.latitude())
                .longitude(coordinate.longitude())
                .build();
        return companyRepository.save(company);
    }

    public Page<Company> searchCompanies(String keyword, CompanyType type, UUID hubId, Pageable pageable) {
        return companyRepository.searchCompanies(keyword, type, hubId, pageable);
    }

    public Company getCompany(UUID id) {
        return companyRepository.findById(id).orElseThrow(() -> new BaseException(CompanyErrorCode.COMPANY_NOT_FOUND));
    }

    public HubUsageStatusResponse checkHubUsage(UUID hubId) {
        boolean isCompanyInUse = companyRepository.existsByHubId(hubId);
        return new HubUsageStatusResponse(isCompanyInUse);
    }

    @Transactional
    public Company updateCompany(Company company, CompanyRequest request, CoordinateResponse coordinate) {
        if (coordinate != null) {
            company.updateCoordinate(coordinate.latitude(), coordinate.longitude());
        }

        company.update(request);

        return companyRepository.save(company);
    }

    @Transactional
    public Company deleteCompany(UUID id, CustomUserPrincipal principal) {
        Company company = getCompany(id);

        // Master가 아니면 담당 허브인지 검증
        if (!principal.isAccessibleHub(company.getHubId())) {
            throw new BaseException(CompanyErrorCode.COMPANY_DELETE_DENIED);
        }

        company.softDelete(principal.userId());

        return company;
    }
}
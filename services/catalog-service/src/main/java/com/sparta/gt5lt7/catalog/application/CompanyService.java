package com.sparta.gt5lt7.catalog.application;

import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.repository.CompanyRepository;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.request.CompanyCreateRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyService {
    private final HubClient hubClient;
    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyCreateResponse createCompany(CompanyCreateRequest request) {
        HubResponse hubResponse = hubClient.getHub(request.getHubId());

        Company company = Company.builder()
                .name(request.getName())
                .type(request.getType())
                .phone(request.getPhone())
                .hubId(request.getHubId())
                .baseAddress(request.getBaseAddress())
                .detailAddress(request.getDetailAddress())
                .zipcode(request.getZipcode())
                // TODO: 지도 API 연동 후, BaseAddress를 기반으로 실제 위경도 좌표를 추출해야 함
                .latitude(BigDecimal.valueOf(37.503))
                .longitude(BigDecimal.valueOf(127.044))
                .build();

        Company savedCompany = companyRepository.save(company);
        return CompanyCreateResponse.from(savedCompany, hubResponse);
    }
}
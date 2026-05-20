package com.sparta.gt5lt7.catalog.application;

import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.domain.repository.CompanyRepository;
import com.sparta.gt5lt7.catalog.global.exception.CompanyErrorCode;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.UserClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.request.CompanyCreateRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyCreateResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyDetailResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyResponse;
import com.sparta.gt5lt7.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sparta.gt5lt7.common.exception.BaseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyService {
    private final HubClient hubClient;
    private final UserClient userClient;
    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyCreateResponse createCompany(CompanyCreateRequest request) {
        // Hub Service로 허브 정보 요청
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
        return CompanyCreateResponse.of(savedCompany, hubResponse);
    }

    public PageResponse<CompanyResponse> searchCompanies(String keyword, CompanyType type, UUID hubId, Pageable pageable) {
        Page<Company> companiePage = companyRepository.searchCompanies(keyword, type, hubId, pageable);

        // 허브 ID를 중복 없이 추출 → Hub Service로 허브 정보 요청
        Set<UUID> hubIds = companiePage.stream()
                .map(Company::getHubId)
                .collect(Collectors.toSet());
        List<HubResponse> hubResponses = hubClient.getHubs(hubIds);

        // O(1) 조회를 위한 허브 Map 생성
        Map<UUID, HubResponse> hubMap = hubResponses.stream()
                .collect(Collectors.toMap(HubResponse::getId, Function.identity()));

        return PageResponse.of(companiePage, company -> {
            HubResponse hubResponse = hubMap.get(company.getHubId());
            return CompanyResponse.of(company, hubResponse);
        });
    }

    public CompanyDetailResponse getCompany(UUID id) {
        Company company = getCompanyById(id);

        // Hub Service로 허브 정보 요청
        HubResponse hubResponse = hubClient.getHub(company.getHubId());

        // 사용자 ID를 중복 없이 추출 → User Service로 사용자 정보 요청
        Set<UUID> userIds = Set.of(company.getCreatedBy(), company.getUpdatedBy());
        List<UserResponse> userResponses = userClient.getUsers(userIds);

        // O(1) 조회를 위한 사용자 Map 생성
        Map<UUID, UserResponse> userMap = userResponses.stream()
                .collect(Collectors.toMap(UserResponse::getId, user -> user));

        return CompanyDetailResponse.of(
                company, hubResponse, userMap.get(company.getCreatedBy()), userMap.get(company.getUpdatedBy())
        );
    }

    // 공통 Company 조회 메서드
    private Company getCompanyById(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new BaseException(CompanyErrorCode.COMPANY_NOT_FOUND));
    }
}
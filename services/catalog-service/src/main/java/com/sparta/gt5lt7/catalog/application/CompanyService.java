package com.sparta.gt5lt7.catalog.application;

import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.domain.repository.CompanyRepository;
import com.sparta.gt5lt7.catalog.global.exception.CompanyErrorCode;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.UserClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.request.CompanyRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.*;
import com.sparta.gt5lt7.common.dto.PageResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sparta.gt5lt7.common.exception.BaseException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CompanyService {
    private final HubClient hubClient;
    private final UserClient userClient;
    private final ProductService productService;
    private final KakaoMapService kakaoMapService;
    private final CompanyRepository companyRepository;

    public CompanyService(
            HubClient hubClient, UserClient userClient,
            @Lazy ProductService productService, KakaoMapService kakaoMapService,
            CompanyRepository companyRepository
    ) {
        this.hubClient = hubClient;
        this.userClient = userClient;
        this.productService = productService;
        this.kakaoMapService = kakaoMapService;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public CompanyResponse.Create createCompany(CompanyRequest request, CustomUserPrincipal principal) {
        // Master가 아니면 담당 허브인지 검증
        if (!principal.isAccessibleHub(request.getHubId())) {
            throw new BaseException(CompanyErrorCode.COMPANY_CREATE_DENIED);
        }

        // Kakao Map Service로 좌표 정보 요청
        CoordinateResponse coordinateResponse = kakaoMapService.getCoordinates(request.getBaseAddress());

        Company company = Company.builder()
                .name(request.getName())
                .type(request.getType())
                .phone(request.getPhone())
                .hubId(request.getHubId())
                .baseAddress(request.getBaseAddress())
                .detailAddress(request.getDetailAddress())
                .zipcode(request.getZipcode())
                .latitude(coordinateResponse.latitude())
                .longitude(coordinateResponse.longitude())
                .build();

        Company savedCompany = companyRepository.save(company);
        return CompanyResponse.Create.from(savedCompany);
    }

    public PageResponse<CompanyResponse.Summary> searchCompanies(String keyword, CompanyType type, UUID hubId, Pageable pageable) {
        Page<Company> companyPage = companyRepository.searchCompanies(keyword, type, hubId, pageable);

        // 허브 ID를 중복 없이 추출 → Hub Service로 허브 정보 요청
        Set<UUID> hubIds = companyPage.stream()
                .map(Company::getHubId)
                .collect(Collectors.toSet());
        List<HubResponse> hubResponses = hubIds.isEmpty() ? List.of() : hubClient.getHubs(hubIds);

        // O(1) 조회를 위한 허브 Map 생성
        Map<UUID, HubResponse> hubMap = hubResponses.stream()
                .collect(Collectors.toMap(HubResponse::id, Function.identity()));

        return PageResponse.of(companyPage, company -> {
            HubResponse hubResponse = hubMap.get(company.getHubId());
            return CompanyResponse.Summary.of(company, hubResponse);
        });
    }

    public CompanyResponse.Detail getCompany(UUID id) {
        Company company = getCompanyById(id);

        // Hub Service로 허브 정보 요청
        HubResponse hubResponse = hubClient.getHub(company.getHubId());

        // 사용자 ID를 중복 없이 추출 → User Service로 사용자 정보 요청
        Set<UUID> userIds = Set.of(company.getCreatedBy(), company.getUpdatedBy());
        List<UserResponse> userResponses = userClient.getUsers(userIds);

        // O(1) 조회를 위한 사용자 Map 생성
        Map<UUID, UserResponse> userMap = userResponses.stream()
                .collect(Collectors.toMap(UserResponse::id, user -> user));

        return CompanyResponse.Detail.of(
                company, hubResponse, userMap.get(company.getCreatedBy()), userMap.get(company.getUpdatedBy())
        );
    }

    public HubUsageStatusResponse checkHubUsage(UUID hubId) {
        boolean isCompanyInUse = companyRepository.existsByHubId(hubId);
        return new HubUsageStatusResponse(isCompanyInUse);
    }

    @Transactional
    public CompanyResponse.Update updateCompany(UUID id, CompanyRequest request, CustomUserPrincipal principal) {
        Company company = getCompanyById(id);

        // Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!principal.isAccessibleHub(company.getHubId()) && !principal.isAccessibleCompany(id)) {
            throw new BaseException(CompanyErrorCode.COMPANY_UPDATE_DENIED);
        }

        // 주소 변경 시 Kakao Map Service로 좌표 정보 요청
        if (!company.getBaseAddress().equals(request.getBaseAddress())) {
            CoordinateResponse coordinateResponse = kakaoMapService.getCoordinates(request.getBaseAddress());
            company.updateCoordinate(coordinateResponse.latitude(), coordinateResponse.longitude());
        }

        company.update(request);

        // Hub Service로 허브 정보 요청
        HubResponse hubResponse = hubClient.getHub(company.getHubId());

        return CompanyResponse.Update.of(company, hubResponse);
    }

    @Transactional
    public CompanyResponse.Delete deleteCompany(UUID id, CustomUserPrincipal principal) {
        Company company = getCompanyById(id);

        // Master가 아니면 담당 허브인지 검증
        if (!principal.isAccessibleHub(company.getHubId())) {
            throw new BaseException(CompanyErrorCode.COMPANY_DELETE_DENIED);
        }

        // Soft Delete 처리
        UUID deletedBy = principal.userId();
        company.softDelete(deletedBy);
        productService.deleteProducts(company.getCompanyId(), deletedBy);

        return CompanyResponse.Delete.from(company);
    }

    // 업체 조회 공통 메서드
    public Company getCompanyById(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new BaseException(CompanyErrorCode.COMPANY_NOT_FOUND));
    }
}
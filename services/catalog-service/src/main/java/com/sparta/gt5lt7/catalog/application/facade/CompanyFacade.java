package com.sparta.gt5lt7.catalog.application.facade;

import com.sparta.gt5lt7.catalog.application.service.ProductService;
import com.sparta.gt5lt7.catalog.presentation.dto.response.HubUsageStatusResponse;
import com.sparta.gt5lt7.common.dto.PageResponse;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.infrastructure.client.UserClient;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.catalog.global.exception.CompanyErrorCode;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.catalog.application.service.CompanyService;
import com.sparta.gt5lt7.catalog.application.service.KakaoMapService;
import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.infrastructure.client.HubClient;
import com.sparta.gt5lt7.catalog.presentation.dto.request.CompanyRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CoordinateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CompanyFacade {
    private final CompanyService companyService;
    private final ProductService productService;
    private final KakaoMapService kakaoMapService;
    private final HubClient hubClient;
    private final UserClient userClient;

    public CompanyResponse.Create createCompany(CompanyRequest request, CustomUserPrincipal principal) {
        // [권한 검증] Master가 아니면 담당 허브인지 검증
        if (!principal.isAccessibleHub(request.getHubId())) {
            throw new BaseException(CompanyErrorCode.COMPANY_CREATE_DENIED);
        }

        // [MSA 통신] Hub Service로 허브 정보 요청
        hubClient.getHub(request.getHubId());

        // [외부 API 통신] Kakao Map Service로 좌표 정보 요청
        CoordinateResponse coordinate = kakaoMapService.getCoordinate(request.getBaseAddress());

        // [서비스 레이어] 업체 생성
        Company company = companyService.createCompany(request, coordinate);
        return CompanyResponse.Create.from(company);
    }

    public PageResponse<CompanyResponse.Summary> searchCompanies(String keyword, CompanyType type, UUID hubId, Pageable pageable) {
        // [서비스 레이어] 업체 목록 조회
        Page<Company> companyPage = companyService.searchCompanies(keyword, type, hubId, pageable);

        // [MSA 통신] 허브 ID를 중복 없이 추출 → Hub Service로 허브 정보 요청
        Set<UUID> hubIds = companyPage.stream().map(Company::getHubId).collect(Collectors.toSet());
        List<HubResponse> hubs = hubIds.isEmpty() ? List.of() : hubClient.getHubs(hubIds);

        // O(1) 조회를 위한 허브 Map 생성
        Map<UUID, HubResponse> hubMap = hubs.stream().collect(Collectors.toMap(HubResponse::id, Function.identity()));

        return PageResponse.of(companyPage, company -> {
            HubResponse hub = hubMap.get(company.getHubId());
            return CompanyResponse.Summary.of(company, hub);
        });
    }

    public CompanyResponse.Detail getCompany(UUID id) {
        // [서비스 레이어] 업체 조회
        Company company = companyService.getCompany(id);

        // [MSA 통신] Hub Service로 허브 정보 요청
        HubResponse hub = hubClient.getHub(company.getHubId());

        // [MSA 통신] 사용자 ID를 중복 없이 추출 → User Service로 사용자 정보 요청
        Set<UUID> userIds = Stream.of(company.getCreatedBy(), company.getUpdatedBy())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        List<UserResponse> users = userClient.getUsers(userIds);

        // O(1) 조회를 위한 사용자 Map 생성
        Map<UUID, UserResponse> userMap = users.stream().collect(Collectors.toMap(UserResponse::id, user -> user));

        // 생성자/수정자 정보 처리
        UserResponse createdBy = UserResponse.from(company.getCreatedBy(), userMap);
        UserResponse updatedBy = UserResponse.from(company.getUpdatedBy(), userMap);

        return CompanyResponse.Detail.of(company, hub, createdBy, updatedBy);
    }

    public HubUsageStatusResponse checkHubUsage(UUID hubId) {
        boolean isCompanyInUse = companyService.checkHubUsage(hubId);
        return new HubUsageStatusResponse(isCompanyInUse);
    }

    public CompanyResponse.Update updateCompany(UUID id, CompanyRequest request, CustomUserPrincipal principal) {
        // [서비스 레이어] 업체 조회
        Company company = companyService.getCompany(id);

        // [권한 검증] 허브 변경 시 Master가 아니면 불가
        if (!company.getHubId().equals(request.getHubId()) && !principal.isMaster()) {
            throw new BaseException(CompanyErrorCode.COMPANY_UPDATE_DENIED);
        }

        // [권한 검증] Master가 아니면 담당 허브 또는 본인 업체인지 검증
        if (!principal.isAccessibleHub(company.getHubId()) && !principal.isAccessibleCompany(id)) {
            throw new BaseException(CompanyErrorCode.COMPANY_UPDATE_DENIED);
        }

        // [MSA 통신] Hub Service로 허브 정보 요청
        HubResponse hub = hubClient.getHub(request.getHubId());

        // [외부 API 통신] 주소 변경 시 Kakao Map Service로 좌표 정보 요청
        CoordinateResponse coordinate = null;
        if (!company.getBaseAddress().equals(request.getBaseAddress())) {
            coordinate = kakaoMapService.getCoordinate(request.getBaseAddress());
        }

        // [서비스 레이어] 업체 수정
        Company updatedCompany = companyService.updateCompany(company, request, coordinate);
        return CompanyResponse.Update.of(updatedCompany, hub);
    }

    @Transactional
    public CompanyResponse.Delete deleteCompany(UUID id, CustomUserPrincipal principal) {
        Company company = companyService.deleteCompany(id, principal);
        productService.deleteProducts(company.getCompanyId(), principal.userId());
        return CompanyResponse.Delete.from(company);
    }
}
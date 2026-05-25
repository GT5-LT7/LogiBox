package com.sparta.gt5lt7.catalog.application.facade;

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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompanyFacade {
    private final CompanyService companyService;
    private final KakaoMapService kakaoMapService;
    private final HubClient hubClient;
    private final UserClient userClient;

    public CompanyResponse.Create createCompany(CompanyRequest request, CustomUserPrincipal principal) {
        // [권한 검증] Master가 아니면 담당 허브인지 검증
        if (!principal.isAccessibleHub(request.getHubId())) {
            throw new BaseException(CompanyErrorCode.COMPANY_CREATE_DENIED);
        }

        // [MSA 통신] Hub Service로 허브 정보 요청 (존재하지 않으면 404 예외)
        hubClient.getHub(request.getHubId());

        // [외부 통신] Kakao Map Service로 좌표 정보 요청
        CoordinateResponse coordinate = kakaoMapService.getCoordinates(request.getBaseAddress());

        // [서비스 레이어]
        Company company = companyService.createCompanyEntity(request, coordinate);

        return CompanyResponse.Create.from(company);
    }

    public CompanyResponse.Detail getCompany(UUID id) {
        // [서비스 레이어]
        Company company = companyService.getCompany(id);

        // [MSA 통신] Hub Service로 허브 정보 요청
        HubResponse hub = hubClient.getHub(company.getHubId());

        // 사용자 ID를 중복 없이 추출 → User Service로 사용자 정보 요청
        Set<UUID> userIds = Set.of(company.getCreatedBy(), company.getUpdatedBy());
        List<UserResponse> users = userClient.getUsers(userIds);

        // O(1) 조회를 위한 사용자 Map 생성
        Map<UUID, UserResponse> userMap = users.stream()
                .collect(Collectors.toMap(UserResponse::id, user -> user));

        return CompanyResponse.Detail.of(
                company, hub, userMap.get(company.getCreatedBy()), userMap.get(company.getUpdatedBy())
        );
    }
}
package com.sparta.gt5lt7.catalog.application.facade;

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

@Component
@RequiredArgsConstructor
public class CompanyFacade {
    private final CompanyService companyService;
    private final KakaoMapService kakaoMapService;
    private final HubClient hubClient;

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
}
package com.sparta.gt5lt7.catalog.presentation.dto.response;

import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 업체 상세 정보 응답 DTO 클래스입니다.
 * <p>
 * 부모 클래스({@link CompanyResponse})의 필드까지 포함한 빌더 패턴을 구현하기 위해
 * {@code @SuperBuilder}를 사용합니다.
 * <li>부모 클래스와 자식 클래스 모두 {@code @SuperBuilder} 어노테이션이 적용되어야 정상 동작합니다.</li>
 */
@Getter
@SuperBuilder
public class CompanyDetailResponse extends CompanyResponse {
    private UserResponse createdBy;
    private UserResponse updatedBy;

    public static CompanyDetailResponse of(
            Company company, HubResponse hub, UserResponse createdBy, UserResponse updatedBy
    ) {
        return CompanyDetailResponse.builder()
                .companyId(company.getCompanyId())
                .name(company.getName())
                .type(company.getType())
                .phone(company.getPhone())
                .hub(hub)
                .address(company.getFullAddress())
                .latitude(company.getLatitude())
                .longitude(company.getLongitude())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .build();
    }
}
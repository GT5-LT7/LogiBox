package com.sparta.gt5lt7.catalog.presentation.dto.response;

import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 업체 생성 응답 DTO 클래스입니다.
 */
@Getter
@Builder
@AllArgsConstructor
public final class CompanyCreateResponse {
    private UUID companyId;
    private String name;
    private CompanyType type;
    private String phone;
    private HubResponse hub;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime createdAt;

    public static CompanyCreateResponse of(Company company, HubResponse hubResponse) {
        return CompanyCreateResponse.builder()
                .companyId(company.getCompanyId())
                .name(company.getName())
                .type(company.getType())
                .phone(company.getPhone())
                .hub(hubResponse)
                .address(company.getFullAddress())
                .latitude(company.getLatitude())
                .longitude(company.getLongitude())
                .createdAt(company.getCreatedAt())
                .build();
    }
}
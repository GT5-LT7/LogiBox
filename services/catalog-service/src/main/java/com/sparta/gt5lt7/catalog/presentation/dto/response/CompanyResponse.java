package com.sparta.gt5lt7.catalog.presentation.dto.response;

import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 업체 정보 응답 DTO 클래스입니다.
 * <p>
 * 상속 구조({@link CompanyDetailResponse})에서의 빌더 패턴 지원을 위해
 * {@code @Builder} 대신 {@code @SuperBuilder}를 사용합니다.
 * <li>{@code @SuperBuilder}: 부모-자식 클래스 간의 필드 빌더 공유를 가능하게 합니다.</li>
 * <li>{@code @AllArgsConstructor} 제거: {@code @SuperBuilder}가 내부에 필요한 생성자를 자동으로 생성하므로 제외합니다.</li>
 */
@Getter
@SuperBuilder
public class CompanyResponse {
    private UUID companyId;
    private String name;
    private CompanyType type;
    private String phone;
    private HubResponse hub;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CompanyResponse of(Company company, HubResponse hub) {
        return CompanyResponse.builder()
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
                .build();
    }
}
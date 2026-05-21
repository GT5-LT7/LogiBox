package com.sparta.gt5lt7.catalog.presentation.dto.response;

import com.sparta.gt5lt7.catalog.domain.entity.Company;
import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CompanyResponse {
    public record Simple(UUID companyId, String name) {
        public static Simple from(Company company) {
            return new Simple(company.getCompanyId(), company.getName());
        }
    }

    public record Info(
            UUID companyId, String name, CompanyType type, String phone,
            HubResponse hub, String address,  BigDecimal latitude, BigDecimal longitude
    ) {
        public static Info of(Company company, HubResponse hub) {
            return new Info(
                    company.getCompanyId(), company.getName(), company.getType(), company.getPhone(),
                    hub, company.getFullAddress(), company.getLatitude(), company.getLongitude()
            );
        }
    }

    public record Create(Info info, LocalDateTime createdAt) {
        public static Create of(Company company, HubResponse hub) {
            return new Create(Info.of(company, hub), company.getCreatedAt());
        }
    }

    public record Summary(Info info, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Summary of(Company company, HubResponse hub) {
            return new Summary(Info.of(company, hub), company.getCreatedAt(), company.getUpdatedAt());
        }
    }

    public record Detail(
            Info info, LocalDateTime createdAt, LocalDateTime updatedAt,
            UserResponse createdBy, UserResponse updatedBy
    ) {
        public static Detail of(Company company, HubResponse hub, UserResponse createdBy, UserResponse updatedBy) {
            return new Detail(Info.of(company, hub), company.getCreatedAt(), company.getUpdatedAt(), createdBy, updatedBy);
        }
    }

    public record Update(Info info, LocalDateTime updatedAt) {
        public static Update of(Company company, HubResponse hub) {
            return new Update(Info.of(company, hub), company.getUpdatedAt());
        }
    }

    public record Delete(UUID companyId, String name, LocalDateTime deletedAt) {
        public static Delete from(Company company) {
            return new Delete(company.getCompanyId(), company.getName(), company.getDeletedAt());
        }
    }
}
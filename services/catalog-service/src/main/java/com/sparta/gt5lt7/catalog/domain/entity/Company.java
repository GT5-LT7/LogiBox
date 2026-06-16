package com.sparta.gt5lt7.catalog.domain.entity;

import com.sparta.gt5lt7.catalog.presentation.dto.request.CompanyRequest;
import com.sparta.gt5lt7.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Company extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID companyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CompanyType type;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false)
    private UUID hubId;

    @Column(nullable = false)
    private String baseAddress;

    @Column
    private String detailAddress;

    @Column(nullable = false, length = 10)
    private String zipcode;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Version
    private Long version;

    @Builder
    public Company(
            String name, CompanyType type, String phone, UUID hubId,
            String baseAddress, String detailAddress, String zipcode,
            BigDecimal latitude, BigDecimal longitude
    ) {
        this.name = name.trim();
        this.type = type;
        this.phone = phone;
        this.hubId = hubId;
        this.baseAddress = baseAddress.trim();
        this.detailAddress = (detailAddress != null && !detailAddress.isBlank()) ? detailAddress.trim() : null;
        this.zipcode = zipcode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getFullAddress() {
        if (this.detailAddress == null || this.detailAddress.isBlank()) {
            return String.format("%s (%s)", this.baseAddress, this.zipcode);
        }
        return String.format("%s, %s (%s)", this.baseAddress, this.detailAddress, this.zipcode);
    }

    public void update(CompanyRequest request) {
        String detailAddress = request.detailAddress();

        this.name = request.name().trim();
        this.type = request.type();
        this.phone = request.phone();
        this.hubId = request.hubId();
        this.baseAddress = request.baseAddress().trim();
        this.detailAddress = (detailAddress != null && !detailAddress.isBlank()) ? detailAddress.trim() : null;
        this.zipcode = request.zipcode();
    }

    public void updateCoordinate(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
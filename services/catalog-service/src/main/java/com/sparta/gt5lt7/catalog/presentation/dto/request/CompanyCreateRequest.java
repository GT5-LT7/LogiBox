package com.sparta.gt5lt7.catalog.presentation.dto.request;

import com.sparta.gt5lt7.catalog.domain.entity.CompanyType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * 업체 생성 요청 DTO 클래스입니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CompanyCreateRequest {
    @NotBlank(message = "업체 이름은 필수입니다.")
    @Size(max = 100, message = "업체 이름은 100자 이하로 입력해주세요.")
    private String name;

    @NotNull(message = "업체 유형은 필수입니다.")
    private CompanyType type;

    @NotBlank(message = "업체 전화번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[0-9])[0-9-]{8,20}$", message = "전화번호는 하이픈을 포함하여 20자 이하 숫자로 입력해주세요.")
    private String phone;

    @NotNull(message = "허브 ID는 필수입니다.")
    private UUID hubId;

    @NotBlank(message = "기본 주소는 필수입니다.")
    @Size(max = 255, message = "기본 주소는 255자 이하로 입력해주세요.")
    private String baseAddress;

    @Size(max = 255, message = "상세 주소는 255자 이하로 입력해주세요.")
    private String detailAddress;

    @NotBlank(message = "우편번호는 필수입니다.")
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자로 입력해주세요.")
    private String zipcode;
}
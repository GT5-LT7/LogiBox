package com.sparta.gt5lt7.catalog.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

public class ProductRequest {
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Create {
        @NotBlank(message = "상품 이름은 필수입니다.")
        @Size(max = 100, message = "상품 이름은 100자 이하로 입력해주세요.")
        private String name;

        private String description;

        @NotNull(message = "업체 ID는 필수입니다.")
        private UUID companyId;

        @NotNull(message = "카테고리 ID는 필수입니다.")
        private UUID categoryId;

        @NotNull(message = "가격은 필수입니다.")
        @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
        private Long price;

        @NotNull(message = "재고 수량은 필수입니다.")
        @PositiveOrZero(message = "재고 수량은 0개 이상이어야 합니다.")
        private Integer quantity;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Update {
        @NotBlank(message = "상품 이름은 필수입니다.")
        @Size(max = 100, message = "상품 이름은 100자 이하로 입력해주세요.")
        private String name;

        private String description;

        @NotNull(message = "카테고리 ID는 필수입니다.")
        private UUID categoryId;

        @NotNull(message = "가격은 필수입니다.")
        @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
        private Long price;
    }
}
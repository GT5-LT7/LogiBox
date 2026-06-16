package com.sparta.gt5lt7.catalog.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

public class ProductRequest {
    public record Create(
            @NotBlank(message = "상품 이름은 필수입니다.")
            @Size(max = 100, message = "상품 이름은 100자 이하로 입력해주세요.")
            String name,

            String description,

            @NotNull(message = "업체 ID는 필수입니다.")
            UUID companyId,

            @NotNull(message = "가격은 필수입니다.")
            @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
            Long price,

            @NotNull(message = "재고 수량은 필수입니다.")
            @PositiveOrZero(message = "재고 수량은 0개 이상이어야 합니다.")
            Integer quantity
    ) {}

    @Builder
    public record Update(
            @NotBlank(message = "상품 이름은 필수입니다.")
            @Size(max = 100, message = "상품 이름은 100자 이하로 입력해주세요.")
            String name,

            String description,

            @NotNull(message = "가격은 필수입니다.")
            @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
            Long price
    ) {}

    public record StatusUpdate(
            @NotNull(message = "변경 상태는 필수입니다.")
            ActionType action
    ) {}

    public record StockUpdate(
            @NotNull(message = "변경 재고량은 필수입니다.")
            Integer updateQuantity
    ) {}

    public record OrderStockUpdate(
            @NotNull(message = "주문 ID는 필수입니다.")
            UUID orderId,

            @Valid
            @NotEmpty(message = "변경 상품은 필수입니다.")
            List<StockItem> stockItems
    ) {}

    public record StockItem(
            @NotNull(message = "상품 ID는 필수입니다.")
            UUID productId,

            @NotNull(message = "변경 재고량은 필수입니다.")
            Integer updateQuantity
    ) {}
}
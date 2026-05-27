package com.sparta.gt5lt7.logisticsservice.presentation.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class HubRouteRequest {

    @NotNull(message = "출발 허브 ID는 필수입니다.")
    private UUID fromHubId;

    @NotNull(message = "도착 허브 ID는 필수입니다.")
    private UUID toHubId;

    @NotNull(message = "이동 거리는 필수입니다.")
    @Min(value = 1, message = "이동 거리는 1km 이상이어야 합니다.")
    private Integer distance;

    @NotNull(message = "예상 소요 시간은 필수입니다.")
    @Min(value = 1, message = "예상 소요 시간은 1분 이상이어야 합니다.")
    private Integer duration;

    @AssertTrue(message = "출발 허브와 도착 허브는 동일할 수 없습니다.")
    public boolean isDifferentHubs() {
        if (fromHubId == null || toHubId == null) return true;
        return !fromHubId.equals(toHubId);
    }
}
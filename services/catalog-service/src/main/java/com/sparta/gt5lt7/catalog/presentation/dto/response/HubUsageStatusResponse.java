package com.sparta.gt5lt7.catalog.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 허브 사용 여부 응답 DTO 클래스입니다.
 */
@Getter
@Builder
@AllArgsConstructor
public class HubUsageStatusResponse {
    private boolean used;
}
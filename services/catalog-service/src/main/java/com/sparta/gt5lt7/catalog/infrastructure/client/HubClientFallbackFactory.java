package com.sparta.gt5lt7.catalog.infrastructure.client;

import com.sparta.gt5lt7.catalog.global.exception.HubErrorCode;
import com.sparta.gt5lt7.common.exception.CommonErrorCode;
import com.sparta.gt5lt7.common.exception.BaseException;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class HubClientFallbackFactory implements FallbackFactory<HubClient> {
    @Override
    public HubClient create(Throwable cause) {
        return new HubClient() {
            @Override
            public HubResponse getHub(UUID id) {
                if (cause instanceof FeignException feignException) {
                    // 404 Not Found
                    if (feignException.status() == 404) {
                        log.warn("[HubClient] 조회 대상 허브 없음 - ID: {}", id);
                        throw new BaseException(HubErrorCode.HUB_NOT_FOUND);
                    }
                }

                log.error("[HubClient] 조회 예외 발생 - ID: {}, 원인: {}", id, cause.getMessage());
                throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }

            @Override
            public List<HubResponse> getHubs(Set<UUID> ids) {
                String errorMessage = (cause != null && cause.getMessage() != null) ? cause.getMessage() : "Unknown";
                log.error("[HubClient] 목록 조회 실패로 인한 폴백 실행. 대상 ID: {}, 원인: {}", ids, errorMessage);
                return ids.stream().map(id -> new HubResponse(id, "-")).toList();
            }
        };
    }
}
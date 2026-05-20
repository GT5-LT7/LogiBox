package com.sparta.gt5lt7.catalog.infrastructure.client;

import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class HubClientFallback implements HubClient {
    @Override
    public HubResponse getHub(UUID id) {
        // TODO: /api/hubs/{id} 구현이 완료되면 실제 에러 로깅 및 예외 처리 로직으로 대체
        return new HubResponse(id, "임시 허브");
    }

    @Override
    public List<HubResponse> getHubs(Set<UUID> ids) {
        // TODO: /internal/hubs 구현이 완료되면 실제 에러 로깅 및 예외 처리 로직으로 대체
        return ids.stream()
                .map(id -> new HubResponse(id, "임시 허브"))
                .collect(Collectors.toList());
    }
}
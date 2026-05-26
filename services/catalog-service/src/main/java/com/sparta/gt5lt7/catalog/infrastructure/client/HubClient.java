package com.sparta.gt5lt7.catalog.infrastructure.client;

import com.sparta.gt5lt7.common.config.InternalFeignConfig;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.HubResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@FeignClient(name = "logistics-service", configuration = InternalFeignConfig.class, fallback = HubClientFallback.class)
public interface HubClient {
    @GetMapping("/api/hubs/{id}")
    HubResponse getHub(@PathVariable UUID id);

    @PostMapping("/internal/hubs")
    List<HubResponse> getHubs(@RequestBody Set<UUID> ids);
}
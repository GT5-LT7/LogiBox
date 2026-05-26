package com.sparta.gt5lt7.catalog.infrastructure.client;

import com.sparta.gt5lt7.common.config.InternalFeignConfig;
import com.sparta.gt5lt7.catalog.infrastructure.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@FeignClient(
        name = "user-service",
        configuration = InternalFeignConfig.class,
        fallbackFactory = UserClientFallbackFactory.class
)
public interface UserClient {
    @PostMapping("/internal/users")
    List<UserResponse> getUsers(@RequestBody Set<UUID> ids);
}
package com.sparta.gt5lt7.logisticsservice.presentation;

import com.sparta.gt5lt7.logisticsservice.application.HubService;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.HubRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.HubResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;

    /**
     * Create a new hub from the provided request and return its representation.
     *
     * @param request the hub creation request payload
     * @return a ResponseEntity containing the created HubResponse and HTTP 201 (Created) status
     */
    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<HubResponse> createHub(
            @RequestBody @Valid HubRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hubService.createHub(request));
    }
}
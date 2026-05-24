package com.sparta.gt5lt7.logisticsservice.presentation.dto.request;

import lombok.Getter;

@Getter
public class HubSearchRequest {
    private final String keyword;

    public HubSearchRequest(String keyword) {
        this.keyword = keyword;
    }
}
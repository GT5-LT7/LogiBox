package com.sparta.gt5lt7.logisticsservice.infrastructure.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoDirectionsResponse {

    private List<Route> routes;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        private Summary summary;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {

        private int distance;
        private int duration;
    }

    public boolean isEmpty() {
        return routes == null || routes.isEmpty() || routes.get(0).getSummary() == null;
    }

    public Summary firstSummary() {
        return routes.get(0).getSummary();
    }
}

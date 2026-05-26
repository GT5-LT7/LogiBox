package com.sparta.gt5lt7.logisticsservice.infrastructure.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoLocalResponse {

    private List<Document> documents;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {

        // 카카오 API: x = 경도(longitude), y = 위도(latitude)
        private String x;
        private String y;

        public double getLongitude() { return Double.parseDouble(x); }
        public double getLatitude()  { return Double.parseDouble(y); }
    }

    public boolean isEmpty() {
        return documents == null || documents.isEmpty();
    }

    public Document first() {
        return documents.get(0);
    }
}
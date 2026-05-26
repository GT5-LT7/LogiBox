package com.sparta.gt5lt7.order.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.gt5lt7.order.application.dto.AIDeliveryAnalysisResult;
import com.sparta.gt5lt7.order.domain.entity.RiskLevel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AIResponseParserTest {

    private final AIResponseParser parser = new AIResponseParser(new ObjectMapper());

    @Test
    void AI_응답_JSON_파싱_성공() {
        String response = """
                {
                  "finalDispatchDeadline": "2026-05-24T15:00:00",
                  "estimatedTimeMinutes": 180,
                  "estimatedCost": 30000,
                  "riskLevel": "HIGH"
                }
                """;

        AIDeliveryAnalysisResult result = parser.parse(response);

        assertThat(result.estimatedTimeMinutes()).isEqualTo(180);
        assertThat(result.estimatedCost()).isEqualTo(30000);
        assertThat(result.riskLevel()).isEqualTo(RiskLevel.HIGH);
    }
}

package com.sparta.gt5lt7.order.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.gt5lt7.order.application.dto.AIDeliveryAnalysisResult;
import com.sparta.gt5lt7.order.common.exception.ErrorCode;
import com.sparta.gt5lt7.order.common.exception.OrderException;
import com.sparta.gt5lt7.order.domain.entity.RiskLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AIResponseParser {

    private final ObjectMapper objectMapper;

    public AIDeliveryAnalysisResult parse(String responseText) {
        try {
            String json = extractJson(responseText);

            Parsed parsed = objectMapper.readValue(json, Parsed.class);

            return new AIDeliveryAnalysisResult(
                    LocalDateTime.parse(parsed.finalDispatchDeadline()),
                    parsed.estimatedTimeMinutes(),
                    parsed.estimatedCost(),
                    RiskLevel.valueOf(parsed.riskLevel())
            );
        } catch (Exception e) {
            throw new OrderException(ErrorCode.AI_RESPONSE_PARSE_FAILED, "AI 응답 파싱에 실패했습니다.");
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");

        if (start == -1 || end == -1) {
            throw new OrderException(ErrorCode.AI_RESPONSE_PARSE_FAILED, "AI 응답에서 JSON을 찾을 수 없습니다.");
        }

        return text.substring(start, end + 1);
    }

    private record Parsed(
            String finalDispatchDeadline,
            Integer estimatedTimeMinutes,
            Integer estimatedCost,
            String riskLevel
    ) {
    }
}
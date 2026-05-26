package com.sparta.gt5lt7.order.application.service;

import com.sparta.gt5lt7.order.application.dto.AIDeliveryAnalysisRequest;
import org.springframework.stereotype.Service;

@Service
public class AIPromptService {

    public String createDeliveryAnalysisPrompt(AIDeliveryAnalysisRequest request) {
        return """
                너는 B2B 물류 배송 분석 AI야.
                
                아래 주문/배송 정보를 기반으로 JSON만 응답해.
                
                조건:
                - finalDispatchDeadline은 ISO-8601 LocalDateTime 형식
                - estimatedTimeMinutes는 정수
                - estimatedCost는 정수
                - riskLevel은 LOW, MEDIUM, HIGH 중 하나
                - 설명 문장 없이 JSON만 반환
                
                주문 정보:
                orderId: %s
                deliveryId: %s
                productId: %s
                quantity: %d
                supplierHubName: %s
                receiverHubName: %s
                deliveryDeadline: %s
                
                응답 형식:
                {
                  "finalDispatchDeadline": "2026-05-24T15:00:00",
                  "estimatedTimeMinutes": 180,
                  "estimatedCost": 30000,
                  "riskLevel": "MEDIUM"
                }
                """.formatted(
                request.orderId(),
                request.deliveryId(),
                request.productId(),
                request.quantity(),
                request.supplierHubName(),
                request.receiverHubName(),
                request.deliveryDeadline()
        );
    }
}

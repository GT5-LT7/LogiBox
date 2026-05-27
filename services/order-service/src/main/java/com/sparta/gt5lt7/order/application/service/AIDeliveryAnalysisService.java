package com.sparta.gt5lt7.order.application.service;

import com.sparta.gt5lt7.order.application.dto.AIDeliveryAnalysisRequest;
import com.sparta.gt5lt7.order.application.dto.AIDeliveryAnalysisResult;
import com.sparta.gt5lt7.order.common.exception.ErrorCode;
import com.sparta.gt5lt7.order.common.exception.OrderException;
import com.sparta.gt5lt7.order.domain.entity.AIRequestLog;
import com.sparta.gt5lt7.order.domain.entity.AIType;
import com.sparta.gt5lt7.order.domain.entity.Order;
import com.sparta.gt5lt7.order.domain.entity.RiskLevel;
import com.sparta.gt5lt7.order.domain.repository.AIRequestLogRepository;
import com.sparta.gt5lt7.order.domain.repository.OrderRepository;
import com.sparta.gt5lt7.order.infrastructure.ai.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AIDeliveryAnalysisService {

    private final GeminiClient geminiClient;
    private final AIPromptService aiPromptService;
    private final AIResponseParser aiResponseParser;
    private final AIRequestLogRepository aiRequestLogRepository;
    private final OrderRepository orderRepository;
    private final SlackNotificationService slackNotificationService;

    @Transactional
    public AIDeliveryAnalysisResult analyzeDelivery(AIDeliveryAnalysisRequest request) {
        String prompt = aiPromptService.createDeliveryAnalysisPrompt(request);

        AIRequestLog log = AIRequestLog.builder()
                .deliveryId(request.deliveryId())
                .orderId(request.orderId())
                .slackId(request.slackId())
                .aiType(AIType.DELIVERY_TIME_ESTIMATE)
                .requestPayload(prompt)
                .build();

        aiRequestLogRepository.save(log);

        try {
            String responseText = geminiClient.generate(prompt);
            AIDeliveryAnalysisResult result = aiResponseParser.parse(responseText);

            if (result.riskLevel() == RiskLevel.HIGH) {
                Order order = orderRepository.findById(request.orderId())
                        .orElseThrow();
                slackNotificationService.sendUrgentDelivery(order);
            }

            log.updateResponse(
                    responseText,
                    result.finalDispatchDeadline(),
                    result.estimatedTimeMinutes(),
                    result.estimatedCost(),
                    result.riskLevel()
            );

            return result;

        } catch (Exception e) {
            log.updateResponse(
                    e.getMessage(),
                    null,
                    null,
                    null,
                    null
            );

            throw new OrderException(ErrorCode.AI_API_FAILED);
        }
    }
}
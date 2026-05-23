package com.sparta.gt5lt7.order.domain.repository;

import com.sparta.gt5lt7.order.domain.entity.AIRequestLog;
import com.sparta.gt5lt7.order.domain.entity.AIType;
import com.sparta.gt5lt7.order.domain.entity.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AIRequestLogRepository extends JpaRepository<AIRequestLog, UUID> {

    List<AIRequestLog> findByOrderId(UUID orderId);

    List<AIRequestLog> findByDeliveryId(UUID deliveryId);

    List<AIRequestLog> findBySlackId(UUID slackId);

    List<AIRequestLog> findByAiType(AIType aiType);

    List<AIRequestLog> findByRiskLevel(RiskLevel riskLevel);
}

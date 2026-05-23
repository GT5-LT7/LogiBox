package com.sparta.gt5lt7.order.infrastructure.messaging;

import com.sparta.gt5lt7.order.application.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "order.exchange";
    private static final String ROUTING_KEY = "order.created";

    public void publishOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
}

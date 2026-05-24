package com.sparta.gt5lt7.order.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Queue deliveryCreatedQueue() {
        return QueueBuilder.durable("delivery.created.queue").build();
    }

    @Bean
    public Queue deliveryFailedQueue() {
        return QueueBuilder.durable("delivery.failed.queue").build();
    }

    @Bean
    public Queue deliveryStartedQueue() {
        return QueueBuilder.durable("delivery.started.queue").build();
    }

    @Bean
    public Queue deliveryCompletedQueue() {
        return QueueBuilder
                .durable("delivery.completed.queue")
                .build();
    }
}
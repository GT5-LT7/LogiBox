package com.sparta.gt5lt7.order.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMqConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_CANCELED_QUEUE = "order.canceled.queue";
    public static final String ORDER_CANCELED_ROUTING_KEY = "order.canceled";

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

    @Bean
    public Queue orderCanceledQueue() {
        return QueueBuilder.durable(ORDER_CANCELED_QUEUE).build();
    }

    @Bean
    public Binding orderCanceledBinding() {
        return BindingBuilder
                .bind(orderCanceledQueue())
                .to(orderExchange())
                .with(ORDER_CANCELED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate =
                new RabbitTemplate(connectionFactory);

        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        // Zipkin Trace 전파 활성화
        rabbitTemplate.setObservationEnabled(true);

        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        // Consumer Trace 전파 활성화
        factory.setObservationEnabled(true);

        return factory;
    }
}
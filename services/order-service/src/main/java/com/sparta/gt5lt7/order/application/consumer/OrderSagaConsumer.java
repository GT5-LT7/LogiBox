package com.sparta.gt5lt7.order.application.consumer;

import com.sparta.gt5lt7.order.application.event.DeliveryCreatedEvent;
import com.sparta.gt5lt7.order.application.event.DeliveryFailedEvent;
import com.sparta.gt5lt7.order.application.event.DeliveryStartedEvent;
import com.sparta.gt5lt7.order.application.event.DeliveryCompletedEvent;
import com.sparta.gt5lt7.order.application.service.OrderSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaConsumer {

    private final OrderSagaService orderSagaService;

    @RabbitListener(queues = "delivery.created.queue")
    public void consumeDeliveryCreated(DeliveryCreatedEvent event) {

        log.info("배송 생성 성공 이벤트 수신 orderId={}", event.orderId());

        orderSagaService.handleDeliveryCreated(
                event.orderId(),
                event.deliveryId()
        );
    }

    @RabbitListener(queues = "delivery.failed.queue")
    public void consumeDeliveryFailed(DeliveryFailedEvent event) {

        log.info("배송 생성 실패 이벤트 수신 orderId={}", event.orderId());

        orderSagaService.handleDeliveryFailed(
                event.orderId(),
                event.reason()
        );
    }

    @RabbitListener(queues = "delivery.started.queue")
    public void consumeDeliveryStarted(DeliveryStartedEvent event) {

        log.info("배송 시작 이벤트 수신 orderId={}", event.orderId());

        orderSagaService.handleDeliveryStarted(
                event.orderId()
        );
    }

    @RabbitListener(queues = "delivery.completed.queue")
    public void consumeDeliveryCompleted(DeliveryCompletedEvent event) {

        log.info("배송 완료 이벤트 수신 orderId={}", event.orderId());

        orderSagaService.handleDeliveryCompleted(
                event.orderId()
        );
    }
}
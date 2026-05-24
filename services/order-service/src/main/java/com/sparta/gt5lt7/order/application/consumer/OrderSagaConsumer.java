package com.sparta.gt5lt7.order.application.consumer;

import com.sparta.gt5lt7.order.application.event.DeliveryCompletedEvent;
import com.sparta.gt5lt7.order.application.event.DeliveryCreatedEvent;
import com.sparta.gt5lt7.order.application.event.DeliveryFailedEvent;
import com.sparta.gt5lt7.order.application.event.DeliveryStartedEvent;
import com.sparta.gt5lt7.order.application.service.OrderSagaService;
import com.sparta.gt5lt7.order.common.exception.ErrorCode;
import com.sparta.gt5lt7.order.common.exception.OrderException;
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

        try {

            log.info("배송 생성 성공 이벤트 수신 orderId={}", event.orderId());

            orderSagaService.handleDeliveryCreated(
                    event.orderId(),
                    event.deliveryId()
            );

        } catch (Exception e) {

            log.error("배송 생성 이벤트 처리 실패 orderId={}", event.orderId(), e);

            throw new OrderException(ErrorCode.RABBITMQ_MESSAGE_FAILED);
        }
    }

    @RabbitListener(queues = "delivery.failed.queue")
    public void consumeDeliveryFailed(DeliveryFailedEvent event) {

        try {

            log.info("배송 생성 실패 이벤트 수신 orderId={}", event.orderId());

            orderSagaService.handleDeliveryFailed(
                    event.orderId(),
                    event.reason()
            );

        } catch (Exception e) {

            log.error("배송 실패 이벤트 처리 실패 orderId={}", event.orderId(), e);

            throw new OrderException(ErrorCode.RABBITMQ_MESSAGE_FAILED);
        }
    }

    @RabbitListener(queues = "delivery.started.queue")
    public void consumeDeliveryStarted(DeliveryStartedEvent event) {

        try {

            log.info("배송 시작 이벤트 수신 orderId={}", event.orderId());

            orderSagaService.handleDeliveryStarted(
                    event.orderId()
            );

        } catch (Exception e) {

            log.error("배송 시작 이벤트 처리 실패 orderId={}", event.orderId(), e);

            throw new OrderException(ErrorCode.RABBITMQ_MESSAGE_FAILED);
        }
    }

    @RabbitListener(queues = "delivery.completed.queue")
    public void consumeDeliveryCompleted(DeliveryCompletedEvent event) {

        try {

            log.info("배송 완료 이벤트 수신 orderId={}", event.orderId());

            orderSagaService.handleDeliveryCompleted(
                    event.orderId()
            );

        } catch (Exception e) {

            log.error("배송 완료 이벤트 처리 실패 orderId={}", event.orderId(), e);

            throw new OrderException(ErrorCode.RABBITMQ_MESSAGE_FAILED);
        }
    }
}
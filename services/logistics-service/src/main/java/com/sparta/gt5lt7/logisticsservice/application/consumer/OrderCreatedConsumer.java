package com.sparta.gt5lt7.logisticsservice.application.consumer;

import com.sparta.gt5lt7.logisticsservice.application.event.OrderCreatedEvent;
import com.sparta.gt5lt7.logisticsservice.application.service.DeliveryCreateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final DeliveryCreateService deliveryCreateService;

    @RabbitListener(queues = "order.created.queue")
    public void consume(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신: orderId={}", event.orderId());

        deliveryCreateService.createDeliveryFromOrder(event);

        log.info("배송 자동 생성 완료: orderId={}", event.orderId());
    }
}

package com.sparta.gt5lt7.order.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.gt5lt7.order.application.service.OrderService;
import com.sparta.gt5lt7.order.domain.entity.OrderStatus;
import com.sparta.gt5lt7.order.presentation.dto.response.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean OrderService orderService;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void 주문_단건_조회_성공() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.getOrder(eq(orderId), any()))
                .thenReturn(new OrderResponse(
                        orderId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        1,
                        null,
                        null,
                        LocalDateTime.now().plusDays(1),
                        OrderStatus.PENDING
                ));

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void 주문_목록_조회_성공() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
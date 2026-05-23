package com.sparta.gt5lt7.catalog.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.gt5lt7.catalog.application.ProductService;
import com.sparta.gt5lt7.catalog.presentation.dto.request.ProductRequest;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CategoryResponse;
import com.sparta.gt5lt7.catalog.presentation.dto.response.ProductResponse;
import com.sparta.gt5lt7.common.config.WebConfig;
import com.sparta.gt5lt7.common.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({WebConfig.class, SecurityConfig.class})
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Nested
    @DisplayName("게이트웨이 인증 필터 동작 테스트")
    class GatewayAuthenticationFilterTest {
        @Test
        @DisplayName("성공: 게이트웨이 보안 헤더가 있으면 인증에 성공해 201 반환")
        void test1() throws Exception {
            // given
            String userId = UUID.randomUUID().toString();
            ProductRequest.Create request = ProductRequest.Create.builder()
                    .name("테스트 상품")
                    .companyId(UUID.randomUUID())
                    .categoryId(UUID.randomUUID())
                    .price(10000L)
                    .quantity(100)
                    .build();

            CategoryResponse.Simple category = new CategoryResponse.Simple(UUID.randomUUID(), "테스트 카테고리");
            ProductResponse.Info info = new ProductResponse.Info(
                    UUID.randomUUID(), "테스트 상품", "설명", null,
                    null, null, category, 10000L, 100
            );
            ProductResponse.Create response = new ProductResponse.Create(info, LocalDateTime.now());

            when(productService.createProduct(any(), any())).thenReturn(response);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "ROLE_MASTER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("실패: 보안 헤더가 없으면 시큐리티 필터에서 인증에 실패해 403 반환")
        void test2() throws Exception {
            // given
            ProductRequest.Create request = ProductRequest.Create.builder()
                    .name("테스트 상품")
                    .companyId(UUID.randomUUID())
                    .categoryId(UUID.randomUUID())
                    .price(10000L)
                    .quantity(100)
                    .build();

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.valueOf(request)))
                    .andExpect(status().isForbidden());
        }
    }
}
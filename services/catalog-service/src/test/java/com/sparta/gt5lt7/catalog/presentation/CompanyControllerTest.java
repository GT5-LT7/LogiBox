package com.sparta.gt5lt7.catalog.presentation;

import com.sparta.gt5lt7.common.security.SecurityConfig;
import com.sparta.gt5lt7.catalog.application.CompanyService;
import com.sparta.gt5lt7.catalog.presentation.dto.response.CompanyResponse;
import com.sparta.gt5lt7.common.config.WebConfig;
import com.sparta.gt5lt7.common.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WithMockUser
@Import(WebConfig.class)
@WebMvcTest(CompanyController.class)
class CompanyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @Nested
    @DisplayName("커스텀 페이징 리졸버 동작 테스트")
    class CustomPageableArgumentResolverTest {
        @Test
        @DisplayName("성공: 유효한 페이지 크기와 정렬 조건이라면 그대로 요청")
        void test1() throws Exception {
            // given
            PageResponse<CompanyResponse.Summary> mockResponse = createSummaryResponse(30, "updatedAt: DESC");
            given(companyService.searchCompanies(any(), any(), any(), any())).willReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/companies")
                            .param("size", "30")
                            .param("sort", "updatedAt,asc"))
                    .andExpect(status().isOk());

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(companyService).searchCompanies(any(), any(), any(), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();

            assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
            assertThat(capturedPageable.getPageSize()).isEqualTo(30);
            assertThat(Objects.requireNonNull(capturedPageable.getSort().getOrderFor("updatedAt")).getDirection())
                    .isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("예외: 유효하지 않는 페이지 크기와 정렬 조건이라면 기본값 적용")
        void test2() throws Exception {
            // given
            PageResponse<CompanyResponse.Summary> mockResponse = createSummaryResponse(10, "createdAt: DESC");
            given(companyService.searchCompanies(any(), any(), any(), any())).willReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/companies")
                            .param("size", "20")
                            .param("sort", "name,asc"))
                    .andExpect(status().isOk());

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(companyService).searchCompanies(any(), any(), any(), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();

            assertThat(capturedPageable.getPageSize()).isEqualTo(10);
            assertThat(Objects.requireNonNull(capturedPageable.getSort().getOrderFor("createdAt")).getDirection())
                    .isEqualTo(Sort.Direction.DESC);
        }

        private PageResponse<CompanyResponse.Summary> createSummaryResponse(int size, String sort) {
            return PageResponse.<CompanyResponse.Summary>builder()
                    .content(List.of())
                    .page(0)
                    .size(size)
                    .totalElements(0L)
                    .totalPages(0)
                    .sort(sort)
                    .build();
        }
    }
}
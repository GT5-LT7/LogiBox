package com.sparta.gt5lt7.common.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sort;

    public static <T, E> PageResponse<T> from(Page<E> page, Function<E, T> converter) {
        List<T> convertedContent = page.getContent().stream()
                .map(converter)
                .collect(Collectors.toList());

        return PageResponse.<T>builder()
                .content(convertedContent)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .sort(page.getSort().toString())
                .build();
    }
}
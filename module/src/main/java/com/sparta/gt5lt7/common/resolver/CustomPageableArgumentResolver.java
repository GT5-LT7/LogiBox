package com.sparta.gt5lt7.common.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

public class CustomPageableArgumentResolver implements HandlerMethodArgumentResolver {
    private final List<Integer> ALLOWED_SIZES = List.of(10, 30, 50);
    private final List<String> ALLOWED_SORTS = List.of("createdAt", "updatedAt");

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        // 1. 페이지 번호 (기본값 0)
        String pageStr = webRequest.getParameter("page");
        int page = (pageStr != null) ? Math.max(0, Integer.parseInt(pageStr)) : 0;

        // 2. 페이지 크기 (기본값: 10)
        String sizeStr = webRequest.getParameter("size");
        int size = 10;
        if (sizeStr != null) {
            int inputSize = Integer.parseInt(sizeStr);
            if (ALLOWED_SIZES.contains(inputSize)) {
                size = inputSize;
            }
        }

        // 3. 정렬 기준 (기본값: createdAt,desc)
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        String sortStr = webRequest.getParameter("sort");

        if (sortStr != null && !sortStr.isBlank()) {
            String[] sortParams = sortStr.split(",");
            String sortProperty = sortParams[0].trim();

            if (ALLOWED_SORTS.contains(sortProperty)) {
                Sort.Direction direction = Sort.Direction.DESC;
                if (sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1].trim())) {
                    direction = Sort.Direction.ASC;
                }
                sort = Sort.by(direction, sortProperty);
            }
        }

        return PageRequest.of(page, size, sort);
    }
}

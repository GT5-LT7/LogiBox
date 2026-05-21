package com.sparta.gt5lt7.catalog.presentation.dto.response;

import com.sparta.gt5lt7.catalog.domain.entity.Category;

import java.util.UUID;

public class CategoryResponse {
    public record Simple(UUID categoryId, String name) {
        public static Simple from(Category category) {
            return new Simple(category.getCategoryId(), category.getName());
        }
    }
}
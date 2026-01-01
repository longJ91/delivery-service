package jjh.delivery.adapter.in.web.product.dto;

import jjh.delivery.domain.category.Category;

import java.util.List;

/**
 * 카테고리 목록 응답
 */
public record CategoryListResponse(
        List<CategoryResponse> categories
) {
    public static CategoryListResponse from(List<Category> categories) {
        return new CategoryListResponse(
                categories.stream()
                        .map(CategoryResponse::from)
                        .toList()
        );
    }
}

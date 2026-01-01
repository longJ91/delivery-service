package jjh.delivery.adapter.in.web.product.dto;

import jjh.delivery.domain.category.Category;

import java.util.List;

/**
 * 카테고리 응답
 */
public record CategoryResponse(
        String id,
        String name,
        int depth,
        List<CategoryResponse> children
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDepth(),
                category.getChildren().stream()
                        .map(CategoryResponse::from)
                        .toList()
        );
    }
}

package jjh.delivery.adapter.in.web.product.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 상품 목록 응답
 */
public record ProductListResponse(
        List<ProductListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static ProductListResponse from(Page<ProductListItemResponse> productPage) {
        return new ProductListResponse(
                productPage.getContent(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }
}

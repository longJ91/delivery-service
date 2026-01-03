package jjh.delivery.adapter.in.web.product.dto;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;

import java.util.List;

/**
 * 상품 목록 응답 (커서 기반 페이지네이션)
 */
public record ProductListResponse(
        List<ProductListItemResponse> content,
        int size,
        boolean hasNext,
        String nextCursor
) {
    public static ProductListResponse from(CursorPageResponse<ProductListItemResponse> cursorPage) {
        return new ProductListResponse(
                cursorPage.content(),
                cursorPage.size(),
                cursorPage.hasNext(),
                cursorPage.nextCursor()
        );
    }
}

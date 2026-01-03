package jjh.delivery.adapter.in.web.product.dto;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;

import java.util.List;
import java.util.Map;

/**
 * 상품 리뷰 목록 응답 (커서 기반 페이지네이션)
 */
public record ReviewListResponse(
        List<ReviewResponse> content,
        int size,
        boolean hasNext,
        String nextCursor,
        Map<Integer, Long> ratingDistribution
) {
    public static ReviewListResponse from(
            CursorPageResponse<ReviewResponse> cursorPage,
            Map<Integer, Long> ratingDistribution
    ) {
        return new ReviewListResponse(
                cursorPage.content(),
                cursorPage.size(),
                cursorPage.hasNext(),
                cursorPage.nextCursor(),
                ratingDistribution
        );
    }
}

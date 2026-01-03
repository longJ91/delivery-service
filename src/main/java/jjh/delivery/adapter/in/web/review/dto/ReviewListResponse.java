package jjh.delivery.adapter.in.web.review.dto;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.review.Review;

import java.util.List;

/**
 * 리뷰 목록 응답 (커서 기반 페이지네이션)
 */
public record ReviewListResponse(
        List<ReviewDetailResponse> reviews,
        int size,
        boolean hasNext,
        String nextCursor
) {
    public static ReviewListResponse from(CursorPageResponse<Review> cursorPage) {
        List<ReviewDetailResponse> reviews = cursorPage.content().stream()
                .map(ReviewDetailResponse::from)
                .toList();

        return new ReviewListResponse(
                reviews,
                cursorPage.size(),
                cursorPage.hasNext(),
                cursorPage.nextCursor()
        );
    }
}

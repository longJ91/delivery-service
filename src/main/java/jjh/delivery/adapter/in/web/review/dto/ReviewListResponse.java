package jjh.delivery.adapter.in.web.review.dto;

import jjh.delivery.domain.review.Review;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 리뷰 목록 응답
 */
public record ReviewListResponse(
        List<ReviewDetailResponse> reviews,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static ReviewListResponse from(Page<Review> page) {
        List<ReviewDetailResponse> reviews = page.getContent().stream()
                .map(ReviewDetailResponse::from)
                .toList();

        return new ReviewListResponse(
                reviews,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}

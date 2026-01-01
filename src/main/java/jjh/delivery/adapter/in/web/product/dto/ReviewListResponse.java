package jjh.delivery.adapter.in.web.product.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 리뷰 목록 응답
 */
public record ReviewListResponse(
        List<ReviewResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        Map<Integer, Long> ratingDistribution
) {
    public static ReviewListResponse from(
            Page<ReviewResponse> reviewPage,
            Map<Integer, Long> ratingDistribution
    ) {
        return new ReviewListResponse(
                reviewPage.getContent(),
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                ratingDistribution
        );
    }
}

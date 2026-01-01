package jjh.delivery.adapter.in.web.review.dto;

import jjh.delivery.application.port.in.ManageReviewUseCase.ReviewRatingInfo;

import java.util.Map;

/**
 * 리뷰 평점 정보 응답
 */
public record ReviewRatingInfoResponse(
        double averageRating,
        long totalCount,
        Map<Integer, Long> ratingDistribution
) {
    public static ReviewRatingInfoResponse from(ReviewRatingInfo info) {
        return new ReviewRatingInfoResponse(
                info.averageRating(),
                info.totalCount(),
                info.ratingDistribution()
        );
    }
}

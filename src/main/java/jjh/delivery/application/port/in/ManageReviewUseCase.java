package jjh.delivery.application.port.in;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.review.Review;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Review Use Case - Driving Port (Inbound)
 */
public interface ManageReviewUseCase {

    /**
     * 리뷰 작성
     */
    Review createReview(CreateReviewCommand command);

    /**
     * 리뷰 수정
     */
    Review updateReview(UpdateReviewCommand command);

    /**
     * 리뷰 삭제
     */
    void deleteReview(UUID reviewId, UUID customerId);

    /**
     * 리뷰 조회
     */
    Review getReview(UUID reviewId);

    /**
     * 상품별 리뷰 목록 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Review> getReviewsByProductId(UUID productId, String cursor, int size);

    /**
     * 내 리뷰 목록 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Review> getMyReviews(UUID customerId, String cursor, int size);

    /**
     * 상품별 평점 정보
     */
    ReviewRatingInfo getProductRatingInfo(UUID productId);

    /**
     * 판매자 답글 추가
     */
    Review addReply(UUID reviewId, UUID sellerId, String content);

    /**
     * 판매자 답글 수정
     */
    Review updateReply(UUID reviewId, UUID sellerId, String content);

    /**
     * 판매자 답글 삭제
     */
    Review deleteReply(UUID reviewId, UUID sellerId);

    // ==================== Commands ====================

    record CreateReviewCommand(
            String customerId,
            String orderId,
            String sellerId,
            String productId,
            int rating,
            String content,
            List<String> imageUrls
    ) {
        public CreateReviewCommand {
            if (customerId == null || customerId.isBlank()) {
                throw new IllegalArgumentException("고객 ID는 필수입니다");
            }
            if (orderId == null || orderId.isBlank()) {
                throw new IllegalArgumentException("주문 ID는 필수입니다");
            }
            if (sellerId == null || sellerId.isBlank()) {
                throw new IllegalArgumentException("판매자 ID는 필수입니다");
            }
            if (productId == null || productId.isBlank()) {
                throw new IllegalArgumentException("상품 ID는 필수입니다");
            }
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("평점은 1~5 사이여야 합니다");
            }
        }
    }

    record UpdateReviewCommand(
            String reviewId,
            String customerId,
            int rating,
            String content,
            List<String> imageUrls
    ) {
        public UpdateReviewCommand {
            if (reviewId == null || reviewId.isBlank()) {
                throw new IllegalArgumentException("리뷰 ID는 필수입니다");
            }
            if (customerId == null || customerId.isBlank()) {
                throw new IllegalArgumentException("고객 ID는 필수입니다");
            }
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("평점은 1~5 사이여야 합니다");
            }
        }
    }

    record ReviewRatingInfo(
            double averageRating,
            long totalCount,
            Map<Integer, Long> ratingDistribution
    ) {}
}

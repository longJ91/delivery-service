package jjh.delivery.application.port.in;

import jjh.delivery.domain.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

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
    void deleteReview(String reviewId, String customerId);

    /**
     * 리뷰 조회
     */
    Review getReview(String reviewId);

    /**
     * 상품별 리뷰 목록 조회
     */
    Page<Review> getReviewsByProductId(String productId, Pageable pageable);

    /**
     * 내 리뷰 목록 조회
     */
    Page<Review> getMyReviews(String customerId, Pageable pageable);

    /**
     * 상품별 평점 정보
     */
    ReviewRatingInfo getProductRatingInfo(String productId);

    /**
     * 판매자 답글 추가
     */
    Review addReply(String reviewId, String sellerId, String content);

    /**
     * 판매자 답글 수정
     */
    Review updateReply(String reviewId, String sellerId, String content);

    /**
     * 판매자 답글 삭제
     */
    Review deleteReply(String reviewId, String sellerId);

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

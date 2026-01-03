package jjh.delivery.application.port.out;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.review.Review;

import java.util.Optional;
import java.util.UUID;

/**
 * Review 조회 Port - Driven Port (Outbound)
 * Note: getAverageRatingByProductId, getRatingDistributionByProductId는 LoadReviewStatsPort로 분리됨
 */
public interface LoadReviewPort {

    /**
     * ID로 리뷰 조회
     */
    Optional<Review> findById(UUID reviewId);

    /**
     * 상품별 리뷰 목록 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Review> findByProductId(UUID productId, String cursor, int size);

    /**
     * 상품별 리뷰 수 조회
     */
    long countByProductId(UUID productId);

    /**
     * 고객별 리뷰 목록 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Review> findByCustomerId(UUID customerId, String cursor, int size);

    /**
     * 판매자별 리뷰 목록 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Review> findBySellerId(UUID sellerId, String cursor, int size);

    /**
     * 주문에 대한 리뷰 존재 여부 확인
     */
    boolean existsByOrderId(UUID orderId);
}

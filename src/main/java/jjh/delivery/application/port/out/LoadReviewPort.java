package jjh.delivery.application.port.out;

import jjh.delivery.domain.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Review 조회 Port - Driven Port (Outbound)
 * Note: getAverageRatingByProductId, getRatingDistributionByProductId는 LoadReviewStatsPort로 분리됨
 */
public interface LoadReviewPort {

    /**
     * ID로 리뷰 조회
     */
    Optional<Review> findById(String reviewId);

    /**
     * 상품별 리뷰 목록 조회
     */
    Page<Review> findByProductId(String productId, Pageable pageable);

    /**
     * 상품별 리뷰 수 조회
     */
    long countByProductId(String productId);

    /**
     * 고객별 리뷰 목록 조회
     */
    Page<Review> findByCustomerId(String customerId, Pageable pageable);

    /**
     * 판매자별 리뷰 목록 조회
     */
    Page<Review> findBySellerId(String sellerId, Pageable pageable);

    /**
     * 주문에 대한 리뷰 존재 여부 확인
     */
    boolean existsByOrderId(String orderId);
}

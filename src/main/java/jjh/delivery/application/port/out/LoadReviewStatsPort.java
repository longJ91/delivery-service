package jjh.delivery.application.port.out;

import java.util.Map;
import java.util.UUID;

/**
 * Load Review Stats Port - Driven Port (Outbound)
 * 리뷰 통계 조회를 위한 포트
 * jOOQ Adapter에서 구현 (컴파일 타임 타입 안전성)
 */
public interface LoadReviewStatsPort {

    /**
     * 상품별 평균 평점 조회
     */
    double getAverageRatingByProductId(UUID productId);

    /**
     * 상품별 평점 분포 조회
     */
    Map<Integer, Long> getRatingDistributionByProductId(UUID productId);
}

package jjh.delivery.application.port.out;

import java.util.UUID;

/**
 * Load Product Stats Port - Driven Port (Outbound)
 * 상품 통계 조회를 위한 포트
 * jOOQ Adapter에서 구현 (컴파일 타임 타입 안전성)
 */
public interface LoadProductStatsPort {

    /**
     * 카테고리별 활성 상품 수 조회
     */
    long countByCategoryId(UUID categoryId);
}

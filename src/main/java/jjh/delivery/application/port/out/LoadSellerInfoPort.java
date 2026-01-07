package jjh.delivery.application.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Load Seller Info Port - Driven Port (Outbound)
 * 판매자 정보 조회를 위한 포트
 * jOOQ Adapter에서 구현 (컴파일 타임 타입 안전성)
 */
public interface LoadSellerInfoPort {

    /**
     * ID로 판매자 상호명 조회
     */
    Optional<String> findBusinessNameById(UUID sellerId);

    /**
     * ID로 판매자 존재 여부 확인
     */
    boolean existsById(UUID sellerId);
}

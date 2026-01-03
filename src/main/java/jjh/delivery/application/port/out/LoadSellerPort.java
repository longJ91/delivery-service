package jjh.delivery.application.port.out;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.seller.Seller;
import jjh.delivery.domain.seller.SellerStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Seller 조회 Port - Driven Port (Outbound)
 * Note: findBusinessNameById는 LoadSellerInfoPort로 분리됨
 */
public interface LoadSellerPort {

    /**
     * 판매자 존재 여부 확인
     */
    boolean existsById(UUID sellerId);

    /**
     * ID로 판매자 조회
     */
    Optional<Seller> findById(UUID sellerId);

    /**
     * 사업자번호로 판매자 조회
     */
    Optional<Seller> findByBusinessNumber(String businessNumber);

    /**
     * 이메일로 판매자 조회
     */
    Optional<Seller> findByEmail(String email);

    /**
     * 전체 판매자 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Seller> findAll(String cursor, int size);

    /**
     * 상태별 판매자 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Seller> findByStatus(SellerStatus status, String cursor, int size);

    /**
     * 사업자번호 존재 여부 확인
     */
    boolean existsByBusinessNumber(String businessNumber);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
}

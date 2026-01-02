package jjh.delivery.application.port.out;

import jjh.delivery.domain.seller.Seller;
import jjh.delivery.domain.seller.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Seller 조회 Port - Driven Port (Outbound)
 * Note: findBusinessNameById는 LoadSellerInfoPort로 분리됨
 */
public interface LoadSellerPort {

    /**
     * 판매자 존재 여부 확인
     */
    boolean existsById(String sellerId);

    /**
     * ID로 판매자 조회
     */
    Optional<Seller> findById(String sellerId);

    /**
     * 사업자번호로 판매자 조회
     */
    Optional<Seller> findByBusinessNumber(String businessNumber);

    /**
     * 이메일로 판매자 조회
     */
    Optional<Seller> findByEmail(String email);

    /**
     * 전체 판매자 조회
     */
    Page<Seller> findAll(Pageable pageable);

    /**
     * 상태별 판매자 조회
     */
    Page<Seller> findByStatus(SellerStatus status, Pageable pageable);

    /**
     * 사업자번호 존재 여부 확인
     */
    boolean existsByBusinessNumber(String businessNumber);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
}

package jjh.delivery.application.port.out;

import jjh.delivery.domain.promotion.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Load Coupon Port - Driven Port (Outbound)
 * 쿠폰 조회 포트
 */
public interface LoadCouponPort {

    Optional<Coupon> findById(UUID couponId);

    Optional<Coupon> findByCode(String code);

    Page<Coupon> findAll(Pageable pageable);

    Page<Coupon> findByActiveStatus(boolean isActive, Pageable pageable);

    List<Coupon> findUsableCoupons();
}

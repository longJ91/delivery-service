package jjh.delivery.application.port.out;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.promotion.Coupon;

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

    CursorPageResponse<Coupon> findAll(String cursor, int size);

    CursorPageResponse<Coupon> findByActiveStatus(boolean isActive, String cursor, int size);

    List<Coupon> findUsableCoupons();
}

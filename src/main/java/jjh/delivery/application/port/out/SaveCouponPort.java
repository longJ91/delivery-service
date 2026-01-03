package jjh.delivery.application.port.out;

import jjh.delivery.domain.promotion.Coupon;

import java.util.UUID;

/**
 * Save Coupon Port - Driven Port (Outbound)
 * 쿠폰 저장 포트
 */
public interface SaveCouponPort {

    Coupon save(Coupon coupon);

    void delete(UUID couponId);
}

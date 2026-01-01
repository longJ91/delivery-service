package jjh.delivery.application.port.out;

import jjh.delivery.domain.promotion.Coupon;

/**
 * Save Coupon Port - Driven Port (Outbound)
 * 쿠폰 저장 포트
 */
public interface SaveCouponPort {

    Coupon save(Coupon coupon);

    void delete(String couponId);
}

package jjh.delivery.domain.promotion.exception;

/**
 * Exception thrown when coupon is not found
 */
public class CouponNotFoundException extends RuntimeException {

    private final String couponId;

    public CouponNotFoundException(String couponId) {
        super("Coupon not found: " + couponId);
        this.couponId = couponId;
    }

    public String getCouponId() {
        return couponId;
    }
}

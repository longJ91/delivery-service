package jjh.delivery.adapter.in.web.coupon.dto;

import jjh.delivery.application.port.in.ManageCouponUseCase.CouponValidationResult;

import java.math.BigDecimal;

/**
 * 쿠폰 유효성 검사 응답
 */
public record CouponValidationResponse(
        boolean valid,
        String couponId,
        String couponCode,
        BigDecimal discountAmount,
        String message
) {
    public static CouponValidationResponse from(CouponValidationResult result) {
        return new CouponValidationResponse(
                result.valid(),
                result.couponId(),
                result.couponCode(),
                result.discountAmount(),
                result.message()
        );
    }
}

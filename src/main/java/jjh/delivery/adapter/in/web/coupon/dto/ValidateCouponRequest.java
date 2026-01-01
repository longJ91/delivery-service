package jjh.delivery.adapter.in.web.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * 쿠폰 유효성 검사 요청
 */
public record ValidateCouponRequest(
        @NotBlank(message = "쿠폰 코드는 필수입니다")
        String couponCode,

        @NotNull(message = "주문 금액은 필수입니다")
        @Positive(message = "주문 금액은 0보다 커야 합니다")
        BigDecimal orderAmount
) {
}

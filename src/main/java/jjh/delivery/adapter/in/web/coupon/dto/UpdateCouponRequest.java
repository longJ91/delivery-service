package jjh.delivery.adapter.in.web.coupon.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 쿠폰 수정 요청
 */
public record UpdateCouponRequest(
        @Size(max = 100, message = "쿠폰명은 100자 이내여야 합니다")
        String name,

        @Size(max = 500, message = "설명은 500자 이내여야 합니다")
        String description,

        @PositiveOrZero(message = "최소 주문 금액은 0 이상이어야 합니다")
        BigDecimal minimumOrderAmount,

        BigDecimal maximumDiscountAmount,

        @PositiveOrZero(message = "발급 수량은 0 이상이어야 합니다")
        int totalQuantity,

        LocalDateTime validFrom,

        LocalDateTime validUntil
) {
}

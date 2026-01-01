package jjh.delivery.adapter.in.web.coupon.dto;

import jakarta.validation.constraints.*;
import jjh.delivery.domain.promotion.CouponScope;
import jjh.delivery.domain.promotion.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 쿠폰 생성 요청
 */
public record CreateCouponRequest(
        @NotBlank(message = "쿠폰 코드는 필수입니다")
        @Size(max = 50, message = "쿠폰 코드는 50자 이내여야 합니다")
        String code,

        @NotBlank(message = "쿠폰명은 필수입니다")
        @Size(max = 100, message = "쿠폰명은 100자 이내여야 합니다")
        String name,

        @Size(max = 500, message = "설명은 500자 이내여야 합니다")
        String description,

        @NotNull(message = "할인 유형은 필수입니다")
        DiscountType discountType,

        @NotNull(message = "할인 값은 필수입니다")
        @Positive(message = "할인 값은 0보다 커야 합니다")
        BigDecimal discountValue,

        @PositiveOrZero(message = "최소 주문 금액은 0 이상이어야 합니다")
        BigDecimal minimumOrderAmount,

        @Positive(message = "최대 할인 금액은 0보다 커야 합니다")
        BigDecimal maximumDiscountAmount,

        CouponScope scope,

        String scopeTargetId,

        @PositiveOrZero(message = "발급 수량은 0 이상이어야 합니다")
        int totalQuantity,

        LocalDateTime validFrom,

        LocalDateTime validUntil
) {
}

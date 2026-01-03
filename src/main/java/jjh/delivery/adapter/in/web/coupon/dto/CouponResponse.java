package jjh.delivery.adapter.in.web.coupon.dto;

import jjh.delivery.domain.promotion.Coupon;
import jjh.delivery.domain.promotion.CouponScope;
import jjh.delivery.domain.promotion.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 쿠폰 응답
 */
public record CouponResponse(
        String id,
        String code,
        String name,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumOrderAmount,
        BigDecimal maximumDiscountAmount,
        CouponScope scope,
        String scopeTargetId,
        int totalQuantity,
        int usedQuantity,
        int remainingQuantity,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        boolean isActive,
        boolean isUsable,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId().toString(),
                coupon.getCode(),
                coupon.getName(),
                coupon.getDescription(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinimumOrderAmount(),
                coupon.getMaximumDiscountAmount(),
                coupon.getScope(),
                coupon.getScopeTargetId() != null ? coupon.getScopeTargetId().toString() : null,
                coupon.getTotalQuantity(),
                coupon.getUsedQuantity(),
                coupon.getRemainingQuantity(),
                coupon.getValidFrom(),
                coupon.getValidUntil(),
                coupon.isActive(),
                coupon.isUsable(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt()
        );
    }
}

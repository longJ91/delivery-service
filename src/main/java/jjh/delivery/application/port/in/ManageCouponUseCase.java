package jjh.delivery.application.port.in;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.promotion.Coupon;
import jjh.delivery.domain.promotion.CouponScope;
import jjh.delivery.domain.promotion.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Manage Coupon Use Case - Driving Port (Inbound)
 * 쿠폰 관리 유스케이스
 */
public interface ManageCouponUseCase {

    // ==================== 쿠폰 생성/수정/삭제 ====================

    /**
     * 쿠폰 생성
     */
    Coupon createCoupon(CreateCouponCommand command);

    /**
     * 쿠폰 수정
     */
    Coupon updateCoupon(UpdateCouponCommand command);

    /**
     * 쿠폰 삭제
     */
    void deleteCoupon(UUID couponId);

    // ==================== 쿠폰 상태 관리 ====================

    /**
     * 쿠폰 활성화
     */
    Coupon activateCoupon(UUID couponId);

    /**
     * 쿠폰 비활성화
     */
    Coupon deactivateCoupon(UUID couponId);

    // ==================== 쿠폰 사용 ====================

    /**
     * 쿠폰 사용 (주문 시)
     */
    Coupon useCoupon(UUID couponId);

    /**
     * 쿠폰 사용 취소 (환불 시)
     */
    Coupon cancelCouponUsage(UUID couponId);

    /**
     * 할인 금액 계산
     */
    BigDecimal calculateDiscount(String couponCode, BigDecimal orderAmount);

    // ==================== 쿠폰 조회 ====================

    /**
     * 쿠폰 조회 (ID)
     */
    Coupon getCoupon(UUID couponId);

    /**
     * 쿠폰 조회 (코드)
     */
    Coupon getCouponByCode(String code);

    /**
     * 전체 쿠폰 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Coupon> getAllCoupons(String cursor, int size);

    /**
     * 활성 쿠폰 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Coupon> getActiveCoupons(String cursor, int size);

    /**
     * 사용 가능한 쿠폰 목록
     */
    List<Coupon> getUsableCoupons();

    /**
     * 쿠폰 유효성 검사
     */
    CouponValidationResult validateCoupon(String couponCode, BigDecimal orderAmount);

    // ==================== Commands ====================

    record CreateCouponCommand(
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
            LocalDateTime validFrom,
            LocalDateTime validUntil
    ) {
        public CreateCouponCommand {
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("Coupon code is required");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Coupon name is required");
            }
            if (discountType == null) {
                throw new IllegalArgumentException("Discount type is required");
            }
            if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Discount value must be positive");
            }
        }
    }

    record UpdateCouponCommand(
            String couponId,
            String name,
            String description,
            BigDecimal minimumOrderAmount,
            BigDecimal maximumDiscountAmount,
            int totalQuantity,
            LocalDateTime validFrom,
            LocalDateTime validUntil
    ) {
        public UpdateCouponCommand {
            if (couponId == null || couponId.isBlank()) {
                throw new IllegalArgumentException("Coupon ID is required");
            }
        }
    }

    // ==================== Result ====================

    record CouponValidationResult(
            boolean valid,
            UUID couponId,
            String couponCode,
            BigDecimal discountAmount,
            String message
    ) {
        public static CouponValidationResult success(Coupon coupon, BigDecimal discountAmount) {
            return new CouponValidationResult(true, coupon.getId(), coupon.getCode(), discountAmount, "Valid coupon");
        }

        public static CouponValidationResult failure(String message) {
            return new CouponValidationResult(false, null, null, BigDecimal.ZERO, message);
        }
    }
}

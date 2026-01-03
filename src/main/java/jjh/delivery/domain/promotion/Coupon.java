package jjh.delivery.domain.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Coupon Aggregate Root
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class Coupon {

    private final UUID id;
    private final String code;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscountAmount;
    private CouponScope scope;
    private UUID scopeTargetId;
    private int totalQuantity;
    private int usedQuantity;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private boolean isActive;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Coupon(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.code = builder.code;
        this.name = builder.name;
        this.description = builder.description;
        this.discountType = builder.discountType;
        this.discountValue = builder.discountValue;
        this.minimumOrderAmount = builder.minimumOrderAmount != null ? builder.minimumOrderAmount : BigDecimal.ZERO;
        this.maximumDiscountAmount = builder.maximumDiscountAmount;
        this.scope = builder.scope != null ? builder.scope : CouponScope.ALL_PRODUCTS;
        this.scopeTargetId = builder.scopeTargetId;
        this.totalQuantity = builder.totalQuantity;
        this.usedQuantity = builder.usedQuantity;
        this.validFrom = builder.validFrom;
        this.validUntil = builder.validUntil;
        this.isActive = builder.isActive;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 쿠폰 사용
     */
    public void use() {
        if (!isUsable()) {
            throw new IllegalStateException("Coupon is not usable");
        }
        this.usedQuantity++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 쿠폰 사용 취소 (환불 시)
     */
    public void cancelUse() {
        if (usedQuantity <= 0) {
            throw new IllegalStateException("No usage to cancel");
        }
        this.usedQuantity--;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 쿠폰 활성화
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 쿠폰 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 할인 금액 계산
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isApplicable(orderAmount)) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = discountType.calculateDiscount(orderAmount, discountValue);
        if (maximumDiscountAmount != null && discount.compareTo(maximumDiscountAmount) > 0) {
            return maximumDiscountAmount;
        }
        return discount;
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 사용 가능 여부
     */
    public boolean isUsable() {
        if (!isActive) return false;
        if (hasQuantityLimit() && getRemainingQuantity() <= 0) return false;
        LocalDateTime now = LocalDateTime.now();
        if (validFrom != null && now.isBefore(validFrom)) return false;
        if (validUntil != null && now.isAfter(validUntil)) return false;
        return true;
    }

    /**
     * 적용 가능 여부
     */
    public boolean isApplicable(BigDecimal orderAmount) {
        if (!isUsable()) return false;
        return orderAmount.compareTo(minimumOrderAmount) >= 0;
    }

    /**
     * 수량 제한 있는지 확인
     */
    public boolean hasQuantityLimit() {
        return totalQuantity > 0;
    }

    /**
     * 남은 수량
     */
    public int getRemainingQuantity() {
        if (!hasQuantityLimit()) return Integer.MAX_VALUE;
        return totalQuantity - usedQuantity;
    }

    // =====================================================
    // Getters
    // =====================================================

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public BigDecimal getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public BigDecimal getMaximumDiscountAmount() {
        return maximumDiscountAmount;
    }

    public CouponScope getScope() {
        return scope;
    }

    public UUID getScopeTargetId() {
        return scopeTargetId;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getUsedQuantity() {
        return usedQuantity;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private UUID id;
        private String code;
        private String name;
        private String description;
        private DiscountType discountType;
        private BigDecimal discountValue;
        private BigDecimal minimumOrderAmount;
        private BigDecimal maximumDiscountAmount;
        private CouponScope scope;
        private UUID scopeTargetId;
        private int totalQuantity;
        private int usedQuantity;
        private LocalDateTime validFrom;
        private LocalDateTime validUntil;
        private boolean isActive = true;
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder discountType(DiscountType discountType) {
            this.discountType = discountType;
            return this;
        }

        public Builder discountValue(BigDecimal discountValue) {
            this.discountValue = discountValue;
            return this;
        }

        public Builder minimumOrderAmount(BigDecimal minimumOrderAmount) {
            this.minimumOrderAmount = minimumOrderAmount;
            return this;
        }

        public Builder maximumDiscountAmount(BigDecimal maximumDiscountAmount) {
            this.maximumDiscountAmount = maximumDiscountAmount;
            return this;
        }

        public Builder scope(CouponScope scope) {
            this.scope = scope;
            return this;
        }

        public Builder scopeTargetId(UUID scopeTargetId) {
            this.scopeTargetId = scopeTargetId;
            return this;
        }

        public Builder totalQuantity(int totalQuantity) {
            this.totalQuantity = totalQuantity;
            return this;
        }

        public Builder usedQuantity(int usedQuantity) {
            this.usedQuantity = usedQuantity;
            return this;
        }

        public Builder validFrom(LocalDateTime validFrom) {
            this.validFrom = validFrom;
            return this;
        }

        public Builder validUntil(LocalDateTime validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Coupon build() {
            validateRequired();
            return new Coupon(this);
        }

        private void validateRequired() {
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("code is required");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
            if (discountType == null) {
                throw new IllegalArgumentException("discountType is required");
            }
            if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("discountValue must be positive");
            }
        }
    }
}

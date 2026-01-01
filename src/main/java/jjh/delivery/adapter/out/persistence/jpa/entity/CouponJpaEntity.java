package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import jjh.delivery.domain.promotion.CouponScope;
import jjh.delivery.domain.promotion.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Coupon JPA Entity
 */
@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupons_code", columnList = "code"),
        @Index(name = "idx_coupons_valid_until", columnList = "valid_until")
})
public class CouponJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount;

    @Column(name = "maximum_discount_amount", precision = 10, scale = 2)
    private BigDecimal maximumDiscountAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponScope scope;

    @Column(name = "scope_target_id", length = 36)
    private String scopeTargetId;

    @Column(name = "total_quantity")
    private int totalQuantity;

    @Column(name = "used_quantity", nullable = false)
    private int usedQuantity;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    protected CouponJpaEntity() {
    }

    public CouponJpaEntity(
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
            LocalDateTime validFrom,
            LocalDateTime validUntil,
            boolean isActive,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minimumOrderAmount = minimumOrderAmount;
        this.maximumDiscountAmount = maximumDiscountAmount;
        this.scope = scope;
        this.scopeTargetId = scopeTargetId;
        this.totalQuantity = totalQuantity;
        this.usedQuantity = usedQuantity;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() {
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

    public String getScopeTargetId() {
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

    public Long getVersion() {
        return version;
    }

    // Setters for update
    public void setUsedQuantity(int usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

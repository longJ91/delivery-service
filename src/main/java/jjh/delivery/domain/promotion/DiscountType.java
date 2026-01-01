package jjh.delivery.domain.promotion;

import java.math.BigDecimal;

/**
 * Discount Type Enum
 */
public enum DiscountType {

    /** 정액 할인 */
    FIXED_AMOUNT,

    /** 정률 할인 */
    PERCENTAGE;

    /**
     * 할인 금액 계산
     */
    public BigDecimal calculateDiscount(BigDecimal originalPrice, BigDecimal discountValue) {
        return switch (this) {
            case FIXED_AMOUNT -> discountValue;
            case PERCENTAGE -> originalPrice.multiply(discountValue.divide(BigDecimal.valueOf(100)));
        };
    }
}

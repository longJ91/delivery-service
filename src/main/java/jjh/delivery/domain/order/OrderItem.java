package jjh.delivery.domain.order;

import java.math.BigDecimal;

/**
 * Order Item Value Object
 */
public record OrderItem(
        String menuId,
        String menuName,
        int quantity,
        BigDecimal unitPrice
) {
    public OrderItem {
        if (menuId == null || menuId.isBlank()) {
            throw new IllegalArgumentException("menuId is required");
        }
        if (menuName == null || menuName.isBlank()) {
            throw new IllegalArgumentException("menuName is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("unitPrice must be positive");
        }
    }

    public BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

package jjh.delivery.domain.order;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Order Item Value Object (v2 - Product Delivery)
 */
public record OrderItem(
        String productId,
        String productName,
        String variantId,
        String variantName,
        String sku,
        Map<String, String> optionValues,
        int quantity,
        BigDecimal unitPrice
) {
    public OrderItem {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId is required");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("productName is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("unitPrice must be positive");
        }
        // Make optionValues immutable
        optionValues = optionValues != null ? Map.copyOf(optionValues) : Map.of();
    }

    /**
     * Factory method for simple product (no variant)
     */
    public static OrderItem of(String productId, String productName, int quantity, BigDecimal unitPrice) {
        return new OrderItem(productId, productName, null, null, null, null, quantity, unitPrice);
    }

    /**
     * Factory method for variant product
     */
    public static OrderItem ofVariant(
            String productId,
            String productName,
            String variantId,
            String variantName,
            String sku,
            Map<String, String> optionValues,
            int quantity,
            BigDecimal unitPrice
    ) {
        return new OrderItem(productId, productName, variantId, variantName, sku, optionValues, quantity, unitPrice);
    }

    public BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean hasVariant() {
        return variantId != null && !variantId.isBlank();
    }
}

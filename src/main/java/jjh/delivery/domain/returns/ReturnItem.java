package jjh.delivery.domain.returns;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Return Item Value Object
 */
public record ReturnItem(
        UUID id,
        UUID orderItemId,
        UUID productId,
        String productName,
        UUID variantId,
        String variantName,
        int quantity,
        BigDecimal refundAmount
) {
    public ReturnItem {
        if (orderItemId == null) {
            throw new IllegalArgumentException("orderItemId is required");
        }
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("refundAmount cannot be negative");
        }
    }

    /**
     * Factory method
     */
    public static ReturnItem of(
            UUID orderItemId,
            UUID productId,
            String productName,
            UUID variantId,
            String variantName,
            int quantity,
            BigDecimal refundAmount
    ) {
        return new ReturnItem(
                UUID.randomUUID(),
                orderItemId,
                productId,
                productName,
                variantId,
                variantName,
                quantity,
                refundAmount
        );
    }

    /**
     * 변형 상품 여부
     */
    public boolean hasVariant() {
        return variantId != null;
    }
}

package jjh.delivery.domain.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cart Item Value Object
 * 장바구니 상품 항목
 */
public record CartItem(
        String id,
        String productId,
        String productName,
        String variantId,
        String variantName,
        String sellerId,
        int quantity,
        BigDecimal unitPrice,
        String thumbnailUrl,
        LocalDateTime addedAt
) {
    public CartItem {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId is required");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
    }

    /**
     * 새 장바구니 항목 생성
     */
    public static CartItem create(
            String productId,
            String productName,
            String variantId,
            String variantName,
            String sellerId,
            int quantity,
            BigDecimal unitPrice,
            String thumbnailUrl
    ) {
        return new CartItem(
                UUID.randomUUID().toString(),
                productId,
                productName,
                variantId,
                variantName,
                sellerId,
                quantity,
                unitPrice,
                thumbnailUrl,
                LocalDateTime.now()
        );
    }

    /**
     * 소계 계산
     */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * 수량 변경
     */
    public CartItem updateQuantity(int newQuantity) {
        return new CartItem(
                id,
                productId,
                productName,
                variantId,
                variantName,
                sellerId,
                newQuantity,
                unitPrice,
                thumbnailUrl,
                addedAt
        );
    }
}

package jjh.delivery.domain.product;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Product Variant Value Object
 * 상품 옵션 조합 (예: 색상-사이즈)
 */
public record ProductVariant(
        UUID id,
        String name,
        String sku,
        Map<String, String> optionValues,
        BigDecimal additionalPrice,
        int stockQuantity,
        boolean isActive
) {
    public ProductVariant {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Variant name is required");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (additionalPrice == null) {
            additionalPrice = BigDecimal.ZERO;
        }
        // Make optionValues immutable
        optionValues = optionValues != null ? Map.copyOf(optionValues) : Map.of();
    }

    /**
     * Factory method
     */
    public static ProductVariant of(
            String name,
            String sku,
            Map<String, String> optionValues,
            BigDecimal additionalPrice,
            int stockQuantity
    ) {
        return new ProductVariant(
                UUID.randomUUID(),
                name,
                sku,
                optionValues,
                additionalPrice,
                stockQuantity,
                true
        );
    }

    /**
     * 재고 있음 여부
     */
    public boolean hasStock() {
        return stockQuantity > 0;
    }

    /**
     * 재고 차감
     */
    public ProductVariant decreaseStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new IllegalArgumentException("Not enough stock. Available: " + stockQuantity);
        }
        return new ProductVariant(id, name, sku, optionValues, additionalPrice, stockQuantity - quantity, isActive);
    }

    /**
     * 재고 추가
     */
    public ProductVariant increaseStock(int quantity) {
        return new ProductVariant(id, name, sku, optionValues, additionalPrice, stockQuantity + quantity, isActive);
    }

    /**
     * 비활성화
     */
    public ProductVariant deactivate() {
        return new ProductVariant(id, name, sku, optionValues, additionalPrice, stockQuantity, false);
    }

    /**
     * 활성화
     */
    public ProductVariant activate() {
        return new ProductVariant(id, name, sku, optionValues, additionalPrice, stockQuantity, true);
    }
}

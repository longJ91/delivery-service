package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Order Item JPA Entity (v2 - Product Delivery)
 */
@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_product_id", columnList = "product_id"),
        @Index(name = "idx_order_items_variant_id", columnList = "variant_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "variant_name", length = 200)
    private String variantName;

    @Column(name = "sku", length = 50)
    private String sku;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "option_values", columnDefinition = "jsonb")
    private Map<String, String> optionValues;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Builder
    public OrderItemJpaEntity(
            UUID id,
            UUID productId,
            String productName,
            UUID variantId,
            String variantName,
            String sku,
            Map<String, String> optionValues,
            int quantity,
            BigDecimal unitPrice
    ) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.variantId = variantId;
        this.variantName = variantName;
        this.sku = sku;
        this.optionValues = optionValues;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /**
     * Factory method for simple product (no variant)
     */
    public static OrderItemJpaEntity of(
            UUID id,
            UUID productId,
            String productName,
            int quantity,
            BigDecimal unitPrice
    ) {
        return new OrderItemJpaEntity(id, productId, productName, null, null, null, null, quantity, unitPrice);
    }

    /**
     * Factory method for variant product
     */
    public static OrderItemJpaEntity ofVariant(
            UUID id,
            UUID productId,
            String productName,
            UUID variantId,
            String variantName,
            String sku,
            Map<String, String> optionValues,
            int quantity,
            BigDecimal unitPrice
    ) {
        return new OrderItemJpaEntity(id, productId, productName, variantId, variantName, sku, optionValues, quantity, unitPrice);
    }

    void setOrder(OrderJpaEntity order) {
        this.order = order;
    }

    /**
     * Check if this item has variant
     */
    public boolean hasVariant() {
        return variantId != null;
    }

    /**
     * Calculate subtotal
     */
    public BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

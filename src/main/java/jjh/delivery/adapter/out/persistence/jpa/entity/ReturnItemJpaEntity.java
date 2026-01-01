package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Return Item JPA Entity
 */
@Entity
@Table(name = "return_items", indexes = {
        @Index(name = "idx_return_items_return_id", columnList = "return_id"),
        @Index(name = "idx_return_items_order_item_id", columnList = "order_item_id")
})
public class ReturnItemJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private ReturnJpaEntity productReturn;

    @Column(name = "order_item_id", nullable = false, length = 36)
    private String orderItemId;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "variant_id", length = 36)
    private String variantId;

    @Column(name = "variant_name", length = 200)
    private String variantName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    protected ReturnItemJpaEntity() {
    }

    public ReturnItemJpaEntity(
            String id,
            String orderItemId,
            String productId,
            String productName,
            String variantId,
            String variantName,
            int quantity,
            BigDecimal refundAmount
    ) {
        this.id = id;
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productName = productName;
        this.variantId = variantId;
        this.variantName = variantName;
        this.quantity = quantity;
        this.refundAmount = refundAmount;
    }

    void setProductReturn(ReturnJpaEntity productReturn) {
        this.productReturn = productReturn;
    }

    // Getters
    public String getId() {
        return id;
    }

    public ReturnJpaEntity getProductReturn() {
        return productReturn;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getVariantId() {
        return variantId;
    }

    public String getVariantName() {
        return variantName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public boolean hasVariant() {
        return variantId != null && !variantId.isBlank();
    }
}

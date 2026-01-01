package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cart Item JPA Entity
 */
@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_items_cart_id", columnList = "cart_id"),
        @Index(name = "idx_cart_items_product_id", columnList = "product_id")
})
public class CartItemJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartJpaEntity cart;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "variant_id", length = 36)
    private String variantId;

    @Column(name = "variant_name", length = 200)
    private String variantName;

    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    protected CartItemJpaEntity() {
    }

    public CartItemJpaEntity(
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
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.variantId = variantId;
        this.variantName = variantName;
        this.sellerId = sellerId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.thumbnailUrl = thumbnailUrl;
        this.addedAt = addedAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public CartJpaEntity getCart() {
        return cart;
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

    public String getSellerId() {
        return sellerId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    // Setters
    void setCart(CartJpaEntity cart) {
        this.cart = cart;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

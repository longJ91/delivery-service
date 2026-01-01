package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Product Variant JPA Entity
 */
@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_product_variants_product_id", columnList = "product_id"),
        @Index(name = "idx_product_variants_sku", columnList = "sku")
})
public class ProductVariantJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductJpaEntity product;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String sku;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "option_values", columnDefinition = "jsonb")
    private Map<String, String> optionValues;

    @Column(name = "additional_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal additionalPrice;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    protected ProductVariantJpaEntity() {
    }

    public ProductVariantJpaEntity(
            String id,
            String name,
            String sku,
            Map<String, String> optionValues,
            BigDecimal additionalPrice,
            int stockQuantity,
            boolean isActive
    ) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.optionValues = optionValues;
        this.additionalPrice = additionalPrice;
        this.stockQuantity = stockQuantity;
        this.isActive = isActive;
    }

    void setProduct(ProductJpaEntity product) {
        this.product = product;
    }

    // Getters
    public String getId() {
        return id;
    }

    public ProductJpaEntity getProduct() {
        return product;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public Map<String, String> getOptionValues() {
        return optionValues;
    }

    public BigDecimal getAdditionalPrice() {
        return additionalPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters for update
    public void setName(String name) {
        this.name = name;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setOptionValues(Map<String, String> optionValues) {
        this.optionValues = optionValues;
    }

    public void setAdditionalPrice(BigDecimal additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}

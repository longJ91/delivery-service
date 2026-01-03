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
 * Product Variant JPA Entity
 */
@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_product_variants_product_id", columnList = "product_id"),
        @Index(name = "idx_product_variants_sku", columnList = "sku")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVariantJpaEntity {

    @Id
    private UUID id;

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

    @Builder
    public ProductVariantJpaEntity(
            UUID id,
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

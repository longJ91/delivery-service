package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import jjh.delivery.domain.product.ProductStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product JPA Entity
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_seller_id", columnList = "seller_id"),
        @Index(name = "idx_products_status", columnList = "status"),
        @Index(name = "idx_products_name", columnList = "name")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantJpaEntity> variants = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "category_id", length = 36)
    private List<String> categoryIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "image_url", length = 500)
    @OrderColumn(name = "display_order")
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "total_stock_quantity", nullable = false)
    private int totalStockQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Long version;

    @Builder
    public ProductJpaEntity(
            String id,
            String sellerId,
            String name,
            String description,
            BigDecimal basePrice,
            ProductStatus status,
            int totalStockQuantity,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.status = status;
        this.totalStockQuantity = totalStockQuantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addVariant(ProductVariantJpaEntity variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariantJpaEntity variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }

    public void clearVariants() {
        variants.clear();
    }

    // Setters for update
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = new ArrayList<>(categoryIds);
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = new ArrayList<>(imageUrls);
    }

    public void setTotalStockQuantity(int totalStockQuantity) {
        this.totalStockQuantity = totalStockQuantity;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}

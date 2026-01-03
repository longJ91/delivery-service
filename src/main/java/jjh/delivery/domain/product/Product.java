package jjh.delivery.domain.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Product Aggregate Root
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class Product {

    private final UUID id;
    private final UUID sellerId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private ProductStatus status;
    private final List<ProductVariant> variants;
    private final List<UUID> categoryIds;
    private final List<String> imageUrls;
    private final Map<String, String> specifications;
    private int totalStockQuantity;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Product(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.sellerId = builder.sellerId;
        this.name = builder.name;
        this.description = builder.description;
        this.basePrice = builder.basePrice;
        this.status = builder.status != null ? builder.status : ProductStatus.DRAFT;
        this.variants = new ArrayList<>(builder.variants);
        this.categoryIds = new ArrayList<>(builder.categoryIds);
        this.imageUrls = new ArrayList<>(builder.imageUrls);
        this.specifications = new HashMap<>(builder.specifications);
        this.totalStockQuantity = calculateTotalStock();
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 상품 정보 업데이트
     */
    public void updateInfo(String name, String description, BigDecimal basePrice) {
        validateEditable();
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 상품 활성화 (판매 시작)
     */
    public void activate() {
        if (this.status == ProductStatus.DELETED) {
            throw new IllegalStateException("Cannot activate deleted product");
        }
        if (variants.isEmpty() && totalStockQuantity == 0) {
            throw new IllegalStateException("Cannot activate product without stock");
        }
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 상품 비활성화 (판매 중지)
     */
    public void deactivate() {
        validateEditable();
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 상품 삭제
     */
    public void delete() {
        this.status = ProductStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 변형 상품 추가
     */
    public void addVariant(ProductVariant variant) {
        validateEditable();
        variants.add(variant);
        updateStockStatus();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 변형 상품 제거
     */
    public void removeVariant(UUID variantId) {
        validateEditable();
        variants.removeIf(v -> v.id().equals(variantId));
        updateStockStatus();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재고 차감
     */
    public void decreaseStock(UUID variantId, int quantity) {
        // Optional.ifPresentOrElse로 변형/단일 상품 분기 (함수형)
        Optional.ofNullable(variantId)
                .ifPresentOrElse(
                        vid -> variants.replaceAll(v ->
                                v.id().equals(vid) ? v.decreaseStock(quantity) : v),
                        () -> {
                            this.totalStockQuantity -= quantity;
                            if (this.totalStockQuantity < 0) {
                                throw new IllegalArgumentException("Not enough stock");
                            }
                        }
                );
        updateStockStatus();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재고 추가
     */
    public void increaseStock(UUID variantId, int quantity) {
        // Optional.ifPresentOrElse로 변형/단일 상품 분기 (함수형)
        Optional.ofNullable(variantId)
                .ifPresentOrElse(
                        vid -> variants.replaceAll(v ->
                                v.id().equals(vid) ? v.increaseStock(quantity) : v),
                        () -> this.totalStockQuantity += quantity
                );
        updateStockStatus();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 카테고리 추가
     */
    public void addCategory(UUID categoryId) {
        validateEditable();
        if (!categoryIds.contains(categoryId)) {
            categoryIds.add(categoryId);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 카테고리 제거
     */
    public void removeCategory(UUID categoryId) {
        validateEditable();
        if (categoryIds.remove(categoryId)) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 이미지 추가
     */
    public void addImage(String imageUrl) {
        validateEditable();
        imageUrls.add(imageUrl);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이미지 제거
     */
    public void removeImage(String imageUrl) {
        validateEditable();
        if (imageUrls.remove(imageUrl)) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 스펙 추가/수정
     */
    public void setSpecification(String key, String value) {
        validateEditable();
        specifications.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Private Methods
    // =====================================================

    private void validateEditable() {
        if (!status.isEditable()) {
            throw new IllegalStateException("Product is not editable in status: " + status);
        }
    }

    private void updateStockStatus() {
        this.totalStockQuantity = calculateTotalStock();
        if (this.status == ProductStatus.ACTIVE && this.totalStockQuantity == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (this.status == ProductStatus.OUT_OF_STOCK && this.totalStockQuantity > 0) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    private int calculateTotalStock() {
        if (variants.isEmpty()) {
            return totalStockQuantity;
        }
        return variants.stream()
                .filter(ProductVariant::isActive)
                .mapToInt(ProductVariant::stockQuantity)
                .sum();
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 변형 상품 조회
     */
    public Optional<ProductVariant> findVariant(UUID variantId) {
        return variants.stream()
                .filter(v -> v.id().equals(variantId))
                .findFirst();
    }

    /**
     * 판매 가능 여부
     */
    public boolean isSellable() {
        return status.isSellable() && totalStockQuantity > 0;
    }

    /**
     * 변형 상품이 있는지 확인
     */
    public boolean hasVariants() {
        return !variants.isEmpty();
    }

    /**
     * 최종 가격 계산 (변형 상품 추가 가격 포함)
     */
    public BigDecimal calculatePrice(UUID variantId) {
        if (variantId == null) {
            return basePrice;
        }
        return findVariant(variantId)
                .map(v -> basePrice.add(v.additionalPrice()))
                .orElse(basePrice);
    }

    // =====================================================
    // Getters
    // =====================================================

    public UUID getId() {
        return id;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    public List<UUID> getCategoryIds() {
        return Collections.unmodifiableList(categoryIds);
    }

    public List<String> getImageUrls() {
        return Collections.unmodifiableList(imageUrls);
    }

    public Map<String, String> getSpecifications() {
        return Collections.unmodifiableMap(specifications);
    }

    public int getTotalStockQuantity() {
        return totalStockQuantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private UUID id;
        private UUID sellerId;
        private String name;
        private String description;
        private BigDecimal basePrice;
        private ProductStatus status;
        private List<ProductVariant> variants = new ArrayList<>();
        private List<UUID> categoryIds = new ArrayList<>();
        private List<String> imageUrls = new ArrayList<>();
        private Map<String, String> specifications = new HashMap<>();
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder sellerId(UUID sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder basePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public Builder status(ProductStatus status) {
            this.status = status;
            return this;
        }

        public Builder variants(List<ProductVariant> variants) {
            this.variants = new ArrayList<>(variants);
            return this;
        }

        public Builder addVariant(ProductVariant variant) {
            this.variants.add(variant);
            return this;
        }

        public Builder categoryIds(List<UUID> categoryIds) {
            this.categoryIds = new ArrayList<>(categoryIds);
            return this;
        }

        public Builder imageUrls(List<String> imageUrls) {
            this.imageUrls = new ArrayList<>(imageUrls);
            return this;
        }

        public Builder specifications(Map<String, String> specifications) {
            this.specifications = new HashMap<>(specifications);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Product build() {
            validateRequired();
            return new Product(this);
        }

        private void validateRequired() {
            if (sellerId == null) {
                throw new IllegalArgumentException("sellerId is required");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
            if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("basePrice must be non-negative");
            }
        }
    }
}

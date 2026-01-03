package jjh.delivery.domain.seller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Seller Aggregate Root
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class Seller {

    private final UUID id;
    private String businessName;
    private String businessNumber;
    private String representativeName;
    private String email;
    private String phoneNumber;
    private SellerType sellerType;
    private SellerStatus status;
    private WarehouseAddress warehouseAddress;
    private final List<UUID> categoryIds;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;

    private Seller(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.businessName = builder.businessName;
        this.businessNumber = builder.businessNumber;
        this.representativeName = builder.representativeName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.sellerType = builder.sellerType != null ? builder.sellerType : SellerType.INDIVIDUAL;
        this.status = builder.status != null ? builder.status : SellerStatus.PENDING;
        this.warehouseAddress = builder.warehouseAddress;
        this.categoryIds = new ArrayList<>(builder.categoryIds);
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.approvedAt = builder.approvedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 판매자 승인
     */
    public void approve() {
        if (this.status != SellerStatus.PENDING) {
            throw new IllegalStateException("Only pending sellers can be approved");
        }
        this.status = SellerStatus.ACTIVE;
        this.approvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 정보 업데이트
     */
    public void updateInfo(String businessName, String representativeName, String email, String phoneNumber) {
        this.businessName = businessName;
        this.representativeName = representativeName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 창고 주소 변경
     */
    public void updateWarehouseAddress(WarehouseAddress warehouseAddress) {
        this.warehouseAddress = warehouseAddress;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 카테고리 추가
     */
    public void addCategory(UUID categoryId) {
        if (!categoryIds.contains(categoryId)) {
            categoryIds.add(categoryId);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 카테고리 제거
     */
    public void removeCategory(UUID categoryId) {
        if (categoryIds.remove(categoryId)) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 계정 정지
     */
    public void suspend() {
        if (!status.canBeSuspended()) {
            throw new IllegalStateException("Cannot suspend seller in status: " + status);
        }
        this.status = SellerStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        if (this.status == SellerStatus.CLOSED || this.status == SellerStatus.PENDING) {
            throw new IllegalStateException("Cannot activate seller in status: " + status);
        }
        this.status = SellerStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 휴면 전환
     */
    public void makeDormant() {
        if (this.status != SellerStatus.ACTIVE) {
            throw new IllegalStateException("Only active sellers can become dormant");
        }
        this.status = SellerStatus.DORMANT;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 폐업
     */
    public void close() {
        if (!status.canClose()) {
            throw new IllegalStateException("Cannot close seller in status: " + status);
        }
        this.status = SellerStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 판매 가능한 상태인지 확인
     */
    public boolean canSell() {
        return status.canSell();
    }

    /**
     * 특정 카테고리에 속하는지 확인
     */
    public boolean hasCategory(UUID categoryId) {
        return categoryIds.contains(categoryId);
    }

    // =====================================================
    // Getters
    // =====================================================

    public UUID getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public SellerType getSellerType() {
        return sellerType;
    }

    public SellerStatus getStatus() {
        return status;
    }

    public WarehouseAddress getWarehouseAddress() {
        return warehouseAddress;
    }

    public List<UUID> getCategoryIds() {
        return Collections.unmodifiableList(categoryIds);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private UUID id;
        private String businessName;
        private String businessNumber;
        private String representativeName;
        private String email;
        private String phoneNumber;
        private SellerType sellerType;
        private SellerStatus status;
        private WarehouseAddress warehouseAddress;
        private List<UUID> categoryIds = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime approvedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder businessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public Builder businessNumber(String businessNumber) {
            this.businessNumber = businessNumber;
            return this;
        }

        public Builder representativeName(String representativeName) {
            this.representativeName = representativeName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder sellerType(SellerType sellerType) {
            this.sellerType = sellerType;
            return this;
        }

        public Builder status(SellerStatus status) {
            this.status = status;
            return this;
        }

        public Builder warehouseAddress(WarehouseAddress warehouseAddress) {
            this.warehouseAddress = warehouseAddress;
            return this;
        }

        public Builder categoryIds(List<UUID> categoryIds) {
            this.categoryIds = new ArrayList<>(categoryIds);
            return this;
        }

        public Builder addCategoryId(UUID categoryId) {
            this.categoryIds.add(categoryId);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder approvedAt(LocalDateTime approvedAt) {
            this.approvedAt = approvedAt;
            return this;
        }

        public Seller build() {
            validateRequired();
            return new Seller(this);
        }

        private void validateRequired() {
            if (businessName == null || businessName.isBlank()) {
                throw new IllegalArgumentException("businessName is required");
            }
            if (businessNumber == null || businessNumber.isBlank()) {
                throw new IllegalArgumentException("businessNumber is required");
            }
            if (representativeName == null || representativeName.isBlank()) {
                throw new IllegalArgumentException("representativeName is required");
            }
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("email is required");
            }
        }
    }
}

package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import jjh.delivery.domain.seller.SellerStatus;
import jjh.delivery.domain.seller.SellerType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seller JPA Entity
 */
@Entity
@Table(name = "sellers", indexes = {
        @Index(name = "idx_sellers_business_number", columnList = "business_number"),
        @Index(name = "idx_sellers_status", columnList = "status")
})
public class SellerJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "business_number", nullable = false, unique = true, length = 20)
    private String businessNumber;

    @Column(name = "representative_name", nullable = false, length = 100)
    private String representativeName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seller_type", nullable = false, length = 20)
    private SellerType sellerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SellerStatus status;

    @Embedded
    private WarehouseAddressEmbeddable warehouseAddress;

    @ElementCollection
    @CollectionTable(
            name = "seller_categories",
            joinColumns = @JoinColumn(name = "seller_id")
    )
    @Column(name = "category_id", length = 36)
    private List<String> categoryIds = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Long version;

    protected SellerJpaEntity() {
    }

    public SellerJpaEntity(
            String id,
            String businessName,
            String businessNumber,
            String representativeName,
            String email,
            String phoneNumber,
            SellerType sellerType,
            SellerStatus status,
            WarehouseAddressEmbeddable warehouseAddress,
            List<String> categoryIds,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime approvedAt
    ) {
        this.id = id;
        this.businessName = businessName;
        this.businessNumber = businessNumber;
        this.representativeName = representativeName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.sellerType = sellerType;
        this.status = status;
        this.warehouseAddress = warehouseAddress;
        this.categoryIds = new ArrayList<>(categoryIds);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.approvedAt = approvedAt;
    }

    // Getters
    public String getId() {
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

    public WarehouseAddressEmbeddable getWarehouseAddress() {
        return warehouseAddress;
    }

    public List<String> getCategoryIds() {
        return categoryIds;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public Long getVersion() {
        return version;
    }

    // Setters for update
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setRepresentativeName(String representativeName) {
        this.representativeName = representativeName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setStatus(SellerStatus status) {
        this.status = status;
    }

    public void setWarehouseAddress(WarehouseAddressEmbeddable warehouseAddress) {
        this.warehouseAddress = warehouseAddress;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = new ArrayList<>(categoryIds);
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}

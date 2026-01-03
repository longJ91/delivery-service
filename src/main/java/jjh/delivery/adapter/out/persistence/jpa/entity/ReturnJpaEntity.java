package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import jjh.delivery.domain.returns.ReturnReason;
import jjh.delivery.domain.returns.ReturnStatus;
import jjh.delivery.domain.returns.ReturnType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Return JPA Entity
 */
@Entity
@Table(name = "returns", indexes = {
        @Index(name = "idx_returns_order_id", columnList = "order_id"),
        @Index(name = "idx_returns_customer_id", columnList = "customer_id"),
        @Index(name = "idx_returns_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReturnJpaEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_type", nullable = false, length = 20)
    private ReturnType returnType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnReason reason;

    @Column(name = "reason_detail", length = 500)
    private String reasonDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnStatus status;

    @OneToMany(mappedBy = "productReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnItemJpaEntity> items = new ArrayList<>();

    @Column(name = "total_refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalRefundAmount;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    private Long version;

    @Builder
    public ReturnJpaEntity(
            UUID id,
            UUID orderId,
            UUID customerId,
            ReturnType returnType,
            ReturnReason reason,
            String reasonDetail,
            ReturnStatus status,
            BigDecimal totalRefundAmount,
            String rejectReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.returnType = returnType;
        this.reason = reason;
        this.reasonDetail = reasonDetail;
        this.status = status;
        this.totalRefundAmount = totalRefundAmount;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    public void addItem(ReturnItemJpaEntity item) {
        items.add(item);
        item.setProductReturn(this);
    }

    public void clearItems() {
        items.clear();
    }

    // Setters for update
    public void setStatus(ReturnStatus status) {
        this.status = status;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

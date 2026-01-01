package jjh.delivery.domain.returns;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ProductReturn Aggregate Root (반품/교환)
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class ProductReturn {

    private final String id;
    private final String orderId;
    private final String customerId;
    private ReturnType returnType;
    private ReturnReason reason;
    private String reasonDetail;
    private ReturnStatus status;
    private final List<ReturnItem> items;
    private BigDecimal totalRefundAmount;
    private String rejectReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    private ProductReturn(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.returnType = builder.returnType;
        this.reason = builder.reason;
        this.reasonDetail = builder.reasonDetail;
        this.status = builder.status != null ? builder.status : ReturnStatus.REQUESTED;
        this.items = new ArrayList<>(builder.items);
        this.totalRefundAmount = calculateTotalRefundAmount();
        this.rejectReason = builder.rejectReason;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.completedAt = builder.completedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 반품 승인
     */
    public void approve() {
        validateStatusTransition(ReturnStatus.APPROVED);
        this.status = ReturnStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 수거 예정
     */
    public void schedulePickup() {
        validateStatusTransition(ReturnStatus.PICKUP_SCHEDULED);
        this.status = ReturnStatus.PICKUP_SCHEDULED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 수거 완료
     */
    public void pickUp() {
        validateStatusTransition(ReturnStatus.PICKED_UP);
        this.status = ReturnStatus.PICKED_UP;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 검수 시작
     */
    public void startInspection() {
        validateStatusTransition(ReturnStatus.INSPECTING);
        this.status = ReturnStatus.INSPECTING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 반품 완료
     */
    public void complete() {
        validateStatusTransition(ReturnStatus.COMPLETED);
        this.status = ReturnStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 반품 거절
     */
    public void reject(String rejectReason) {
        if (this.status != ReturnStatus.REQUESTED && this.status != ReturnStatus.INSPECTING) {
            throw new IllegalStateException("Can only reject from REQUESTED or INSPECTING status");
        }
        this.status = ReturnStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 반품 취소
     */
    public void cancel() {
        if (!status.isCancellable()) {
            throw new IllegalStateException("Cannot cancel return in status: " + status);
        }
        this.status = ReturnStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Private Methods
    // =====================================================

    private void validateStatusTransition(ReturnStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from " + status + " to " + newStatus);
        }
    }

    private BigDecimal calculateTotalRefundAmount() {
        return items.stream()
                .map(ReturnItem::refundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 판매자 귀책 사유인지 확인
     */
    public boolean isSellerFault() {
        return reason.isSellerFault();
    }

    /**
     * 환불 타입인지 확인
     */
    public boolean isRefund() {
        return returnType.isRefund();
    }

    /**
     * 교환 타입인지 확인
     */
    public boolean isExchange() {
        return returnType.isExchange();
    }

    // =====================================================
    // Getters
    // =====================================================

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public ReturnReason getReason() {
        return reason;
    }

    public String getReasonDetail() {
        return reasonDetail;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public List<ReturnItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private String id;
        private String orderId;
        private String customerId;
        private ReturnType returnType;
        private ReturnReason reason;
        private String reasonDetail;
        private ReturnStatus status;
        private List<ReturnItem> items = new ArrayList<>();
        private String rejectReason;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder returnType(ReturnType returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder reason(ReturnReason reason) {
            this.reason = reason;
            return this;
        }

        public Builder reasonDetail(String reasonDetail) {
            this.reasonDetail = reasonDetail;
            return this;
        }

        public Builder status(ReturnStatus status) {
            this.status = status;
            return this;
        }

        public Builder items(List<ReturnItem> items) {
            this.items = new ArrayList<>(items);
            return this;
        }

        public Builder addItem(ReturnItem item) {
            this.items.add(item);
            return this;
        }

        public Builder rejectReason(String rejectReason) {
            this.rejectReason = rejectReason;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public ProductReturn build() {
            validateRequired();
            return new ProductReturn(this);
        }

        private void validateRequired() {
            if (orderId == null || orderId.isBlank()) {
                throw new IllegalArgumentException("orderId is required");
            }
            if (customerId == null || customerId.isBlank()) {
                throw new IllegalArgumentException("customerId is required");
            }
            if (returnType == null) {
                throw new IllegalArgumentException("returnType is required");
            }
            if (reason == null) {
                throw new IllegalArgumentException("reason is required");
            }
            if (items.isEmpty()) {
                throw new IllegalArgumentException("At least one item is required");
            }
        }
    }
}

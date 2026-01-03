package jjh.delivery.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Aggregate Root
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class Payment {

    private final UUID id;
    private final UUID orderId;
    private PaymentMethodType paymentMethodType;
    private String paymentGateway;
    private String transactionId;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private PaymentStatus status;
    private String failureReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;

    private Payment(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.orderId = builder.orderId;
        this.paymentMethodType = builder.paymentMethodType;
        this.paymentGateway = builder.paymentGateway;
        this.transactionId = builder.transactionId;
        this.amount = builder.amount;
        this.refundedAmount = builder.refundedAmount != null ? builder.refundedAmount : BigDecimal.ZERO;
        this.status = builder.status != null ? builder.status : PaymentStatus.PENDING;
        this.failureReason = builder.failureReason;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.paidAt = builder.paidAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 결제 완료
     */
    public void complete(String transactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Can only complete payment in PENDING status");
        }
        this.transactionId = transactionId;
        this.status = PaymentStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 결제 실패
     */
    public void fail(String reason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Can only fail payment in PENDING status");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 결제 취소
     */
    public void cancel() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Can only cancel payment in PENDING status");
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 부분 환불
     */
    public void refundPartially(BigDecimal refundAmount) {
        if (!status.isRefundable()) {
            throw new IllegalStateException("Cannot refund payment in status: " + status);
        }
        BigDecimal remainingAmount = amount.subtract(refundedAmount);
        if (refundAmount.compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException("Refund amount exceeds remaining amount");
        }
        this.refundedAmount = this.refundedAmount.add(refundAmount);
        if (this.refundedAmount.compareTo(this.amount) >= 0) {
            this.status = PaymentStatus.FULLY_REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 전체 환불
     */
    public void refundFully() {
        if (!status.isRefundable()) {
            throw new IllegalStateException("Cannot refund payment in status: " + status);
        }
        this.refundedAmount = this.amount;
        this.status = PaymentStatus.FULLY_REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 환불 가능 금액
     */
    public BigDecimal getRefundableAmount() {
        return amount.subtract(refundedAmount);
    }

    /**
     * 결제 완료 여부
     */
    public boolean isPaid() {
        return status.isCompleted();
    }

    // =====================================================
    // Getters
    // =====================================================

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public PaymentMethodType getPaymentMethodType() {
        return paymentMethodType;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private UUID id;
        private UUID orderId;
        private PaymentMethodType paymentMethodType;
        private String paymentGateway;
        private String transactionId;
        private BigDecimal amount;
        private BigDecimal refundedAmount;
        private PaymentStatus status;
        private String failureReason;
        private LocalDateTime createdAt;
        private LocalDateTime paidAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder orderId(UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder paymentMethodType(PaymentMethodType paymentMethodType) {
            this.paymentMethodType = paymentMethodType;
            return this;
        }

        public Builder paymentGateway(String paymentGateway) {
            this.paymentGateway = paymentGateway;
            return this;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder refundedAmount(BigDecimal refundedAmount) {
            this.refundedAmount = refundedAmount;
            return this;
        }

        public Builder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder paidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
            return this;
        }

        public Payment build() {
            validateRequired();
            return new Payment(this);
        }

        private void validateRequired() {
            if (orderId == null) {
                throw new IllegalArgumentException("orderId is required");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("amount must be positive");
            }
            if (paymentMethodType == null) {
                throw new IllegalArgumentException("paymentMethodType is required");
            }
        }
    }
}

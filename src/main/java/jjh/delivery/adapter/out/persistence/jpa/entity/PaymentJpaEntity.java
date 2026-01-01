package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import jjh.delivery.domain.payment.PaymentMethodType;
import jjh.delivery.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment JPA Entity
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_order_id", columnList = "order_id"),
        @Index(name = "idx_payments_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_payments_status", columnList = "status")
})
public class PaymentJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method_type", nullable = false, length = 20)
    private PaymentMethodType paymentMethodType;

    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "refunded_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Version
    private Long version;

    protected PaymentJpaEntity() {
    }

    public PaymentJpaEntity(
            String id,
            String orderId,
            PaymentMethodType paymentMethodType,
            String paymentGateway,
            String transactionId,
            BigDecimal amount,
            BigDecimal refundedAmount,
            PaymentStatus status,
            String failureReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime paidAt
    ) {
        this.id = id;
        this.orderId = orderId;
        this.paymentMethodType = paymentMethodType;
        this.paymentGateway = paymentGateway;
        this.transactionId = transactionId;
        this.amount = amount;
        this.refundedAmount = refundedAmount;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.paidAt = paidAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getOrderId() {
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

    public Long getVersion() {
        return version;
    }

    // Setters for update
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}

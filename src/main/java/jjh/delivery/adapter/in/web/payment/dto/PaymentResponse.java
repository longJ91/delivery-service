package jjh.delivery.adapter.in.web.payment.dto;

import jjh.delivery.domain.payment.Payment;
import jjh.delivery.domain.payment.PaymentMethodType;
import jjh.delivery.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 응답
 */
public record PaymentResponse(
        String id,
        String orderId,
        PaymentMethodType paymentMethodType,
        String paymentGateway,
        String transactionId,
        BigDecimal amount,
        BigDecimal refundedAmount,
        BigDecimal refundableAmount,
        PaymentStatus status,
        String failureReason,
        boolean isPaid,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId().toString(),
                payment.getOrderId().toString(),
                payment.getPaymentMethodType(),
                payment.getPaymentGateway(),
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getRefundedAmount(),
                payment.getRefundableAmount(),
                payment.getStatus(),
                payment.getFailureReason(),
                payment.isPaid(),
                payment.getCreatedAt(),
                payment.getPaidAt()
        );
    }
}

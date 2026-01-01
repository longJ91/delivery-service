package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.PaymentJpaEntity;
import jjh.delivery.domain.payment.Payment;
import org.springframework.stereotype.Component;

/**
 * Payment Persistence Mapper
 * Entity <-> Domain 변환
 */
@Component
public class PaymentPersistenceMapper {

    public Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Payment.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .paymentMethodType(entity.getPaymentMethodType())
                .paymentGateway(entity.getPaymentGateway())
                .transactionId(entity.getTransactionId())
                .amount(entity.getAmount())
                .refundedAmount(entity.getRefundedAmount())
                .status(entity.getStatus())
                .failureReason(entity.getFailureReason())
                .createdAt(entity.getCreatedAt())
                .paidAt(entity.getPaidAt())
                .build();
    }

    public PaymentJpaEntity toEntity(Payment domain) {
        if (domain == null) {
            return null;
        }

        return new PaymentJpaEntity(
                domain.getId(),
                domain.getOrderId(),
                domain.getPaymentMethodType(),
                domain.getPaymentGateway(),
                domain.getTransactionId(),
                domain.getAmount(),
                domain.getRefundedAmount(),
                domain.getStatus(),
                domain.getFailureReason(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getPaidAt()
        );
    }
}

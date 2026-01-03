package jjh.delivery.application.port.out;

import jjh.delivery.domain.payment.Payment;
import jjh.delivery.domain.payment.PaymentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Load Port - Driven Port (Outbound)
 */
public interface LoadPaymentPort {

    Optional<Payment> findById(UUID paymentId);

    Optional<Payment> findByOrderId(UUID orderId);

    List<Payment> findByOrderIdIn(List<UUID> orderIds);

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsByOrderId(UUID orderId);
}

package jjh.delivery.application.port.out;

import jjh.delivery.domain.payment.Payment;
import jjh.delivery.domain.payment.PaymentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Payment Load Port - Driven Port (Outbound)
 */
public interface LoadPaymentPort {

    Optional<Payment> findById(String paymentId);

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByOrderIdIn(List<String> orderIds);

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsByOrderId(String orderId);
}

package jjh.delivery.application.port.out;

import jjh.delivery.domain.payment.Payment;

/**
 * Payment Save Port - Driven Port (Outbound)
 */
public interface SavePaymentPort {

    Payment save(Payment payment);
}

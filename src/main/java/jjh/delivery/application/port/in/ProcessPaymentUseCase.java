package jjh.delivery.application.port.in;

import jjh.delivery.domain.payment.Payment;
import jjh.delivery.domain.payment.PaymentMethodType;

import java.math.BigDecimal;

/**
 * Payment Use Case - Driving Port (Inbound)
 */
public interface ProcessPaymentUseCase {

    /**
     * 결제 요청
     */
    Payment requestPayment(RequestPaymentCommand command);

    /**
     * 결제 확인 (PG 콜백 처리)
     */
    Payment confirmPayment(ConfirmPaymentCommand command);

    /**
     * 결제 실패 처리
     */
    Payment failPayment(String paymentId, String reason);

    /**
     * 결제 조회
     */
    Payment getPayment(String paymentId);

    /**
     * 주문별 결제 조회
     */
    Payment getPaymentByOrderId(String orderId);

    // ==================== Commands ====================

    record RequestPaymentCommand(
            String orderId,
            PaymentMethodType paymentMethodType,
            String paymentGateway,
            BigDecimal amount
    ) {
        public RequestPaymentCommand {
            if (orderId == null || orderId.isBlank()) {
                throw new IllegalArgumentException("주문 ID는 필수입니다");
            }
            if (paymentMethodType == null) {
                throw new IllegalArgumentException("결제 방식은 필수입니다");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다");
            }
        }
    }

    record ConfirmPaymentCommand(
            String paymentId,
            String transactionId
    ) {
        public ConfirmPaymentCommand {
            if (paymentId == null || paymentId.isBlank()) {
                throw new IllegalArgumentException("결제 ID는 필수입니다");
            }
            if (transactionId == null || transactionId.isBlank()) {
                throw new IllegalArgumentException("거래 ID는 필수입니다");
            }
        }
    }
}

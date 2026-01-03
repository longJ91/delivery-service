package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import jjh.delivery.application.port.in.ProcessPaymentUseCase;
import jjh.delivery.application.port.out.LoadPaymentPort;
import jjh.delivery.application.port.out.SavePaymentPort;
import jjh.delivery.domain.payment.Payment;
import jjh.delivery.domain.payment.exception.PaymentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment Service - Application Layer
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService implements ProcessPaymentUseCase {

    private final LoadPaymentPort loadPaymentPort;
    private final SavePaymentPort savePaymentPort;

    @Override
    public Payment requestPayment(RequestPaymentCommand command) {
        UUID orderId = UUID.fromString(command.orderId());
        // 이미 해당 주문에 대한 결제가 존재하는지 확인
        if (loadPaymentPort.existsByOrderId(orderId)) {
            throw new IllegalStateException("이미 해당 주문에 대한 결제가 존재합니다: " + command.orderId());
        }

        Payment payment = Payment.builder()
                .orderId(orderId)
                .paymentMethodType(command.paymentMethodType())
                .paymentGateway(command.paymentGateway())
                .amount(command.amount())
                .build();

        return savePaymentPort.save(payment);
    }

    @Override
    public Payment confirmPayment(ConfirmPaymentCommand command) {
        UUID paymentId = UUID.fromString(command.paymentId());
        Payment payment = loadPaymentPort.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));

        payment.complete(command.transactionId());

        return savePaymentPort.save(payment);
    }

    @Override
    public Payment failPayment(UUID paymentId, String reason) {
        Payment payment = loadPaymentPort.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId.toString()));

        payment.fail(reason);

        return savePaymentPort.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPayment(UUID paymentId) {
        return loadPaymentPort.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(UUID orderId) {
        return loadPaymentPort.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("주문에 대한 결제를 찾을 수 없습니다: " + orderId));
    }
}

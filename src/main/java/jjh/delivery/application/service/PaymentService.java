package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;

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
        // 이미 해당 주문에 대한 결제가 존재하는지 확인
        if (loadPaymentPort.existsByOrderId(command.orderId())) {
            throw new IllegalStateException("이미 해당 주문에 대한 결제가 존재합니다: " + command.orderId());
        }

        Payment payment = Payment.builder()
                .orderId(command.orderId())
                .paymentMethodType(command.paymentMethodType())
                .paymentGateway(command.paymentGateway())
                .amount(command.amount())
                .build();

        return savePaymentPort.save(payment);
    }

    @Override
    public Payment confirmPayment(ConfirmPaymentCommand command) {
        Payment payment = loadPaymentPort.findById(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));

        payment.complete(command.transactionId());

        return savePaymentPort.save(payment);
    }

    @Override
    public Payment failPayment(String paymentId, String reason) {
        Payment payment = loadPaymentPort.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        payment.fail(reason);

        return savePaymentPort.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPayment(String paymentId) {
        return loadPaymentPort.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(String orderId) {
        return loadPaymentPort.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("주문에 대한 결제를 찾을 수 없습니다: " + orderId));
    }
}

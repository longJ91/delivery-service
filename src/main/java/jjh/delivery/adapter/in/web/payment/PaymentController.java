package jjh.delivery.adapter.in.web.payment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.payment.dto.ConfirmPaymentRequest;
import jjh.delivery.adapter.in.web.payment.dto.PaymentResponse;
import jjh.delivery.adapter.in.web.payment.dto.RequestPaymentRequest;
import jjh.delivery.application.port.in.ProcessPaymentUseCase;
import jjh.delivery.application.port.in.ProcessPaymentUseCase.ConfirmPaymentCommand;
import jjh.delivery.application.port.in.ProcessPaymentUseCase.RequestPaymentCommand;
import jjh.delivery.domain.payment.Payment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Payment REST Controller - Driving Adapter (Inbound)
 * 결제 API
 */
@RestController
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final ProcessPaymentUseCase processPaymentUseCase;

    /**
     * 결제 요청
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> requestPayment(
            @Valid @RequestBody RequestPaymentRequest request
    ) {
        RequestPaymentCommand command = new RequestPaymentCommand(
                request.orderId(),
                request.paymentMethodType(),
                request.paymentGateway(),
                request.amount()
        );

        Payment payment = processPaymentUseCase.requestPayment(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    /**
     * 결제 확인 (PG 콜백 후)
     */
    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody ConfirmPaymentRequest request
    ) {
        ConfirmPaymentCommand command = new ConfirmPaymentCommand(
                paymentId,
                request.transactionId()
        );

        Payment payment = processPaymentUseCase.confirmPayment(command);

        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * 결제 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String paymentId
    ) {
        Payment payment = processPaymentUseCase.getPayment(paymentId);

        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * 주문별 결제 조회
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable String orderId
    ) {
        Payment payment = processPaymentUseCase.getPaymentByOrderId(orderId);

        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
}

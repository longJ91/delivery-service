package jjh.delivery.adapter.in.web.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jjh.delivery.domain.payment.PaymentMethodType;

import java.math.BigDecimal;

/**
 * 결제 요청
 */
public record RequestPaymentRequest(

        @NotBlank(message = "주문 ID는 필수입니다")
        String orderId,

        @NotNull(message = "결제 방식은 필수입니다")
        PaymentMethodType paymentMethodType,

        String paymentGateway,

        @NotNull(message = "결제 금액은 필수입니다")
        @Positive(message = "결제 금액은 0보다 커야 합니다")
        BigDecimal amount

) {}

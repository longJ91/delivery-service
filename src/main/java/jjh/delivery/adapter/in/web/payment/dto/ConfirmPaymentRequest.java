package jjh.delivery.adapter.in.web.payment.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 결제 확인 요청 (PG 콜백 후)
 */
public record ConfirmPaymentRequest(

        @NotBlank(message = "거래 ID는 필수입니다")
        String transactionId

) {}

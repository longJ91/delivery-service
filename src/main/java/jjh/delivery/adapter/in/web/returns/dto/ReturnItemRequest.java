package jjh.delivery.adapter.in.web.returns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * 반품 상품 요청
 */
public record ReturnItemRequest(

        @NotBlank(message = "주문 항목 ID는 필수입니다")
        String orderItemId,

        @NotBlank(message = "상품 ID는 필수입니다")
        String productId,

        String productName,

        String variantId,

        String variantName,

        @Positive(message = "수량은 0보다 커야 합니다")
        int quantity,

        @NotNull(message = "환불 금액은 필수입니다")
        @PositiveOrZero(message = "환불 금액은 0 이상이어야 합니다")
        BigDecimal refundAmount

) {}

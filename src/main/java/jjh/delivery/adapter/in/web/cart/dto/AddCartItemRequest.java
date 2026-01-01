package jjh.delivery.adapter.in.web.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 장바구니 상품 추가 요청
 */
public record AddCartItemRequest(

        @NotBlank(message = "상품 ID는 필수입니다")
        String productId,

        String variantId,

        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        int quantity

) {}

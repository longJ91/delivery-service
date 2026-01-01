package jjh.delivery.adapter.in.web.cart.dto;

import jakarta.validation.constraints.Min;

/**
 * 장바구니 수량 변경 요청
 */
public record UpdateCartItemRequest(

        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        int quantity

) {}

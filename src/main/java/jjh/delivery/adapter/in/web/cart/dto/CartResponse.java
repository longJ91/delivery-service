package jjh.delivery.adapter.in.web.cart.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 장바구니 응답
 */
public record CartResponse(
        List<CartItemResponse> items,
        BigDecimal totalAmount,
        int totalItems
) {
    public static CartResponse of(List<CartItemResponse> items, BigDecimal totalAmount, int totalItems) {
        return new CartResponse(items, totalAmount, totalItems);
    }
}

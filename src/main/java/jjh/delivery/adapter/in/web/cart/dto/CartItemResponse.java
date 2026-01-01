package jjh.delivery.adapter.in.web.cart.dto;

import jjh.delivery.domain.cart.CartItem;

import java.math.BigDecimal;

/**
 * 장바구니 항목 응답
 */
public record CartItemResponse(
        String id,
        String productId,
        String productName,
        String variantId,
        String variantName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String thumbnailUrl,
        String sellerId,
        String sellerName,
        int stock,
        boolean isAvailable
) {
    public static CartItemResponse from(CartItem item, String sellerName, int stock, boolean isAvailable) {
        return new CartItemResponse(
                item.id(),
                item.productId(),
                item.productName(),
                item.variantId(),
                item.variantName(),
                item.quantity(),
                item.unitPrice(),
                item.getSubtotal(),
                item.thumbnailUrl(),
                item.sellerId(),
                sellerName,
                stock,
                isAvailable
        );
    }
}

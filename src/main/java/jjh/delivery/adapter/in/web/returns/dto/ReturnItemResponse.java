package jjh.delivery.adapter.in.web.returns.dto;

import jjh.delivery.domain.returns.ReturnItem;

import java.math.BigDecimal;

/**
 * 반품 상품 응답
 */
public record ReturnItemResponse(
        String id,
        String orderItemId,
        String productId,
        String productName,
        String variantId,
        String variantName,
        int quantity,
        BigDecimal refundAmount
) {
    public static ReturnItemResponse from(ReturnItem item) {
        return new ReturnItemResponse(
                item.id(),
                item.orderItemId(),
                item.productId(),
                item.productName(),
                item.variantId(),
                item.variantName(),
                item.quantity(),
                item.refundAmount()
        );
    }
}

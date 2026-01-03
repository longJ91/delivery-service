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
                item.id().toString(),
                item.orderItemId().toString(),
                item.productId().toString(),
                item.productName(),
                item.variantId() != null ? item.variantId().toString() : null,
                item.variantName(),
                item.quantity(),
                item.refundAmount()
        );
    }
}

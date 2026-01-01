package jjh.delivery.adapter.in.web.dto;

import jjh.delivery.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Order Response DTO (v2 - Product Delivery)
 */
public record OrderResponse(
        String id,
        String orderNumber,
        String customerId,
        String sellerId,
        List<OrderItemResponse> items,
        OrderStatus status,
        ShippingAddressResponse shippingAddress,
        BigDecimal subtotalAmount,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String orderMemo,
        String shippingMemo,
        String couponId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime paidAt,
        LocalDateTime confirmedAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt
) {
    public record OrderItemResponse(
            String productId,
            String productName,
            String variantId,
            String variantName,
            String sku,
            Map<String, String> optionValues,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}

    public record ShippingAddressResponse(
            String recipientName,
            String phoneNumber,
            String postalCode,
            String address1,
            String address2,
            String deliveryNote,
            String fullAddress
    ) {}
}

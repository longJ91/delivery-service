package jjh.delivery.adapter.in.web.dto;

import jjh.delivery.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Response DTO
 */
public record OrderResponse(
        String id,
        String customerId,
        String shopId,
        List<OrderItemResponse> items,
        OrderStatus status,
        BigDecimal totalAmount,
        String deliveryAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record OrderItemResponse(
            String menuId,
            String menuName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}
}

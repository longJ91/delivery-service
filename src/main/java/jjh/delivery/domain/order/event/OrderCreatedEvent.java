package jjh.delivery.domain.order.event;

import jjh.delivery.domain.order.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Created Domain Event (v2 - Product Delivery)
 */
public record OrderCreatedEvent(
        String orderId,
        String orderNumber,
        String customerId,
        String sellerId,
        BigDecimal totalAmount,
        String shippingAddress,
        LocalDateTime occurredAt
) implements OrderEvent {

    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getSellerId(),
                order.getTotalAmount(),
                order.getShippingAddress() != null ? order.getShippingAddress().getFullAddress() : null,
                LocalDateTime.now()
        );
    }
}

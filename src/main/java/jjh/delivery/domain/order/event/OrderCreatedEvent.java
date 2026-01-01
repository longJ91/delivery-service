package jjh.delivery.domain.order.event;

import jjh.delivery.domain.order.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Created Domain Event
 */
public record OrderCreatedEvent(
        String orderId,
        String customerId,
        String shopId,
        BigDecimal totalAmount,
        String deliveryAddress,
        LocalDateTime occurredAt
) implements OrderEvent {

    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getShopId(),
                order.calculateTotalAmount(),
                order.getDeliveryAddress(),
                LocalDateTime.now()
        );
    }
}

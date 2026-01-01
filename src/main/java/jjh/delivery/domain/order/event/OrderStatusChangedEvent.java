package jjh.delivery.domain.order.event;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderStatus;

import java.time.LocalDateTime;

/**
 * Order Status Changed Domain Event
 */
public record OrderStatusChangedEvent(
        String orderId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        LocalDateTime occurredAt
) implements OrderEvent {

    public static OrderStatusChangedEvent of(Order order, OrderStatus previousStatus) {
        return new OrderStatusChangedEvent(
                order.getId(),
                previousStatus,
                order.getStatus(),
                LocalDateTime.now()
        );
    }
}

package jjh.delivery.domain.order.event;

import java.time.LocalDateTime;

/**
 * Base Domain Event
 */
public sealed interface OrderEvent permits OrderCreatedEvent, OrderStatusChangedEvent {

    String orderId();
    LocalDateTime occurredAt();
}

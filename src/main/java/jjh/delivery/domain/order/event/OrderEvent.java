package jjh.delivery.domain.order.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Domain Event
 */
public sealed interface OrderEvent permits OrderCreatedEvent, OrderStatusChangedEvent {

    UUID orderId();
    LocalDateTime occurredAt();
}

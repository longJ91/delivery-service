package jjh.delivery.adapter.out.messaging.dto;

import jjh.delivery.domain.order.OrderStatus;
import jjh.delivery.domain.order.event.OrderCreatedEvent;
import jjh.delivery.domain.order.event.OrderStatusChangedEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kafka Message DTOs
 */
public sealed interface OrderEventMessage {

    String orderId();
    String eventType();
    LocalDateTime occurredAt();

    record OrderCreatedMessage(
            String orderId,
            String eventType,
            String customerId,
            String shopId,
            BigDecimal totalAmount,
            String deliveryAddress,
            LocalDateTime occurredAt
    ) implements OrderEventMessage {

        public static OrderCreatedMessage from(OrderCreatedEvent event) {
            return new OrderCreatedMessage(
                    event.orderId(),
                    "ORDER_CREATED",
                    event.customerId(),
                    event.shopId(),
                    event.totalAmount(),
                    event.deliveryAddress(),
                    event.occurredAt()
            );
        }
    }

    record OrderStatusChangedMessage(
            String orderId,
            String eventType,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            LocalDateTime occurredAt
    ) implements OrderEventMessage {

        public static OrderStatusChangedMessage from(OrderStatusChangedEvent event) {
            return new OrderStatusChangedMessage(
                    event.orderId(),
                    "ORDER_STATUS_CHANGED",
                    event.previousStatus(),
                    event.newStatus(),
                    event.occurredAt()
            );
        }
    }
}

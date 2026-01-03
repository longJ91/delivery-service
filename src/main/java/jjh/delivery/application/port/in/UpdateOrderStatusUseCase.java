package jjh.delivery.application.port.in;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderStatus;

import java.util.UUID;

/**
 * Update Order Status Use Case - Driving Port (Inbound)
 * v2 - Product Delivery
 */
public interface UpdateOrderStatusUseCase {

    Order payOrder(UUID orderId);

    Order confirmOrder(UUID orderId);

    Order startPreparing(UUID orderId);

    Order shipOrder(UUID orderId);

    Order cancelOrder(UUID orderId);

    Order requestReturn(UUID orderId);

    Order updateStatus(UUID orderId, OrderStatus newStatus);
}

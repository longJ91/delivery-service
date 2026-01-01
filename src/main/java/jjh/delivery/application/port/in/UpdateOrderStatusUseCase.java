package jjh.delivery.application.port.in;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderStatus;

/**
 * Update Order Status Use Case - Driving Port (Inbound)
 * v2 - Product Delivery
 */
public interface UpdateOrderStatusUseCase {

    Order payOrder(String orderId);

    Order confirmOrder(String orderId);

    Order startPreparing(String orderId);

    Order shipOrder(String orderId);

    Order cancelOrder(String orderId);

    Order requestReturn(String orderId);

    Order updateStatus(String orderId, OrderStatus newStatus);
}

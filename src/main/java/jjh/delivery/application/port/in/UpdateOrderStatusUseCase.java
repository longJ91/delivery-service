package jjh.delivery.application.port.in;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderStatus;

/**
 * Update Order Status Use Case - Driving Port (Inbound)
 */
public interface UpdateOrderStatusUseCase {

    Order acceptOrder(String orderId);

    Order startPreparing(String orderId);

    Order readyForDelivery(String orderId);

    Order cancelOrder(String orderId);

    Order updateStatus(String orderId, OrderStatus newStatus);
}

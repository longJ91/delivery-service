package jjh.delivery.application.port.in;

import jjh.delivery.domain.order.Order;

import java.util.Optional;

/**
 * Get Order Use Case - Driving Port (Inbound)
 */
public interface GetOrderUseCase {

    Optional<Order> getOrder(String orderId);

    Order getOrderOrThrow(String orderId);
}

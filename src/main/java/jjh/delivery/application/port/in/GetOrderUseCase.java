package jjh.delivery.application.port.in;

import jjh.delivery.domain.order.Order;

import java.util.Optional;
import java.util.UUID;

/**
 * Get Order Use Case - Driving Port (Inbound)
 */
public interface GetOrderUseCase {

    Optional<Order> getOrder(UUID orderId);

    Order getOrderOrThrow(UUID orderId);
}

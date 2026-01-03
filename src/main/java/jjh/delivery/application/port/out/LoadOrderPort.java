package jjh.delivery.application.port.out;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Load Order Port - Driven Port (Outbound)
 * 주문 조회를 위한 포트 (JPA/jOOQ 구현)
 */
public interface LoadOrderPort {

    Optional<Order> findById(UUID orderId);

    List<Order> findByCustomerId(UUID customerId);

    List<Order> findBySellerId(UUID sellerId);

    List<Order> findByStatus(OrderStatus status);

    boolean existsById(UUID orderId);
}

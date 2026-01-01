package jjh.delivery.application.port.out;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Load Order Port - Driven Port (Outbound)
 * 주문 조회를 위한 포트 (JPA/jOOQ 구현)
 */
public interface LoadOrderPort {

    Optional<Order> findById(String orderId);

    List<Order> findByCustomerId(String customerId);

    List<Order> findBySellerId(String sellerId);

    List<Order> findByStatus(OrderStatus status);

    boolean existsById(String orderId);
}

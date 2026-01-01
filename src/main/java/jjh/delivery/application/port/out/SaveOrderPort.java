package jjh.delivery.application.port.out;

import jjh.delivery.domain.order.Order;

/**
 * Save Order Port - Driven Port (Outbound)
 * 주문 저장을 위한 포트 (JPA 구현)
 */
public interface SaveOrderPort {

    Order save(Order order);

    void delete(String orderId);
}

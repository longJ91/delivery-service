package jjh.delivery.application.port.out;

import jjh.delivery.domain.order.event.OrderEvent;

/**
 * Order Event Port - Driven Port (Outbound)
 * 주문 이벤트 발행을 위한 포트 (Kafka 구현)
 */
public interface OrderEventPort {

    void publish(OrderEvent event);

    void publishAsync(OrderEvent event);
}

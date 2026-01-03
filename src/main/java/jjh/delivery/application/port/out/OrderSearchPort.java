package jjh.delivery.application.port.out;

import jjh.delivery.application.port.in.SearchOrderUseCase.SearchOrderQuery;
import jjh.delivery.domain.order.Order;

import java.util.List;
import java.util.UUID;

/**
 * Order Search Port - Driven Port (Outbound)
 * 주문 검색을 위한 포트 (Elasticsearch 구현)
 */
public interface OrderSearchPort {

    void index(Order order);

    void delete(UUID orderId);

    List<Order> search(SearchOrderQuery query);

    List<Order> findByCustomerId(UUID customerId);

    List<Order> findBySellerId(UUID sellerId);

    void reindexAll();
}

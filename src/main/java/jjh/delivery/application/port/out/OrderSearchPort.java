package jjh.delivery.application.port.out;

import jjh.delivery.application.port.in.SearchOrderUseCase.SearchOrderQuery;
import jjh.delivery.domain.order.Order;

import java.util.List;

/**
 * Order Search Port - Driven Port (Outbound)
 * 주문 검색을 위한 포트 (Elasticsearch 구현)
 */
public interface OrderSearchPort {

    void index(Order order);

    void delete(String orderId);

    List<Order> search(SearchOrderQuery query);

    List<Order> findByCustomerId(String customerId);

    List<Order> findByShopId(String shopId);

    void reindexAll();
}

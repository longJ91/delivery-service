package jjh.delivery.application.service;

import jjh.delivery.application.port.out.OrderQueryPort;
import jjh.delivery.application.port.out.OrderQueryPort.ComplexQueryCriteria;
import jjh.delivery.application.port.out.OrderQueryPort.OrderStatistics;
import jjh.delivery.application.port.out.OrderQueryPort.ReportCriteria;
import jjh.delivery.domain.order.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Query Service (v2 - Product Delivery)
 * 복잡한 조회 전용 서비스 (jOOQ 활용)
 */
@Service
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderQueryPort orderQueryPort;

    public OrderQueryService(OrderQueryPort orderQueryPort) {
        this.orderQueryPort = orderQueryPort;
    }

    public List<Order> findOrdersWithComplexCriteria(ComplexQueryCriteria criteria) {
        return orderQueryPort.findOrdersWithComplexCriteria(criteria);
    }

    public List<OrderStatistics> getOrderStatistics(
            String sellerId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return orderQueryPort.getOrderStatisticsBySeller(sellerId, from, to);
    }

    public List<Order> generateOrderReport(ReportCriteria criteria) {
        return orderQueryPort.findOrdersForReport(criteria);
    }
}

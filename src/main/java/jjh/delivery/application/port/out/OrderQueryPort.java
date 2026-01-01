package jjh.delivery.application.port.out;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Query Port - Driven Port (Outbound)
 * 복잡한 쿼리를 위한 포트 (jOOQ 구현)
 * v2 - Product Delivery
 */
public interface OrderQueryPort {

    List<Order> findOrdersWithComplexCriteria(ComplexQueryCriteria criteria);

    List<OrderStatistics> getOrderStatisticsBySeller(String sellerId, LocalDateTime from, LocalDateTime to);

    List<Order> findOrdersForReport(ReportCriteria criteria);

    record ComplexQueryCriteria(
            List<String> sellerIds,
            List<OrderStatus> statuses,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String sortBy,
            boolean ascending,
            int offset,
            int limit
    ) {}

    record ReportCriteria(
            String sellerId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            boolean includeItems
    ) {}

    record OrderStatistics(
            String sellerId,
            long totalOrders,
            long completedOrders,
            long cancelledOrders,
            BigDecimal totalRevenue,
            BigDecimal averageOrderAmount
    ) {}
}

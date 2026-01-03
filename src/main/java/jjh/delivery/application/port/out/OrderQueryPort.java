package jjh.delivery.application.port.out;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order Query Port - Driven Port (Outbound)
 * 복잡한 쿼리를 위한 포트 (jOOQ 구현)
 * v2 - Product Delivery
 */
public interface OrderQueryPort {

    List<Order> findOrdersWithComplexCriteria(ComplexQueryCriteria criteria);

    List<OrderStatistics> getOrderStatisticsBySeller(UUID sellerId, LocalDateTime from, LocalDateTime to);

    List<Order> findOrdersForReport(ReportCriteria criteria);

    record ComplexQueryCriteria(
            List<UUID> sellerIds,
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
            UUID sellerId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            boolean includeItems
    ) {}

    record OrderStatistics(
            UUID sellerId,
            long totalOrders,
            long completedOrders,
            long cancelledOrders,
            BigDecimal totalRevenue,
            BigDecimal averageOrderAmount
    ) {}
}

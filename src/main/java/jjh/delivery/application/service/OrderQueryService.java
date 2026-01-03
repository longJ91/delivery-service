package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.application.port.out.OrderQueryPort;
import jjh.delivery.application.port.out.OrderQueryPort.ComplexQueryCriteria;
import jjh.delivery.application.port.out.OrderQueryPort.OrderStatistics;
import jjh.delivery.application.port.out.OrderQueryPort.ReportCriteria;
import jjh.delivery.domain.order.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order Query Service (v2 - Product Delivery)
 * 복잡한 조회 전용 서비스 (jOOQ 활용)
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderQueryPort orderQueryPort;

    /**
     * 커서 기반 복잡한 조건 검색
     */
    public CursorPageResponse<Order> findOrdersWithComplexCriteria(ComplexQueryCriteria criteria) {
        return orderQueryPort.findOrdersWithComplexCriteria(criteria);
    }

    public List<OrderStatistics> getOrderStatistics(
            UUID sellerId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return orderQueryPort.getOrderStatisticsBySeller(sellerId, from, to);
    }

    public List<Order> generateOrderReport(ReportCriteria criteria) {
        return orderQueryPort.findOrdersForReport(criteria);
    }
}

package jjh.delivery.adapter.in.web.dto;

import jjh.delivery.domain.order.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 목록 응답 DTO (커서 기반 페이지네이션)
 */
public record OrderListResponse(
        List<OrderSummaryResponse> content,
        int size,
        boolean hasNext,
        String nextCursor
) {
    /**
     * CursorPageResponse에서 OrderListResponse 생성
     */
    public static OrderListResponse from(CursorPageResponse<Order> cursorPage) {
        return new OrderListResponse(
                cursorPage.content().stream()
                        .map(OrderSummaryResponse::from)
                        .toList(),
                cursorPage.size(),
                cursorPage.hasNext(),
                cursorPage.nextCursor()
        );
    }

    /**
     * 주문 요약 응답 (목록용)
     */
    public record OrderSummaryResponse(
            String id,
            String orderNumber,
            String sellerId,
            String sellerName,
            String status,
            BigDecimal totalAmount,
            int itemCount,
            String thumbnailUrl,
            LocalDateTime createdAt,
            LocalDateTime deliveredAt
    ) {
        public static OrderSummaryResponse from(Order order) {
            return new OrderSummaryResponse(
                    order.getId().toString(),
                    order.getOrderNumber(),
                    order.getSellerId().toString(),
                    null, // sellerName은 별도 조회 필요
                    order.getStatus().name(),
                    order.getTotalAmount(),
                    order.getItems().size(),
                    null, // thumbnailUrl은 첫 번째 상품 이미지
                    order.getCreatedAt(),
                    order.getDeliveredAt()
            );
        }
    }
}

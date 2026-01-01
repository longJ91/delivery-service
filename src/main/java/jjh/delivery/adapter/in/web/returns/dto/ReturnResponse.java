package jjh.delivery.adapter.in.web.returns.dto;

import jjh.delivery.domain.returns.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 반품 응답
 */
public record ReturnResponse(
        String id,
        String orderId,
        String customerId,
        ReturnType returnType,
        ReturnReason reason,
        String reasonDisplayName,
        String reasonDetail,
        boolean isSellerFault,
        ReturnStatus status,
        List<ReturnItemResponse> items,
        BigDecimal totalRefundAmount,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
    public static ReturnResponse from(ProductReturn productReturn) {
        List<ReturnItemResponse> items = productReturn.getItems().stream()
                .map(ReturnItemResponse::from)
                .toList();

        return new ReturnResponse(
                productReturn.getId(),
                productReturn.getOrderId(),
                productReturn.getCustomerId(),
                productReturn.getReturnType(),
                productReturn.getReason(),
                productReturn.getReason().getDisplayName(),
                productReturn.getReasonDetail(),
                productReturn.isSellerFault(),
                productReturn.getStatus(),
                items,
                productReturn.getTotalRefundAmount(),
                productReturn.getRejectReason(),
                productReturn.getCreatedAt(),
                productReturn.getCompletedAt()
        );
    }
}

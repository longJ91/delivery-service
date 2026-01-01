package jjh.delivery.application.port.in;

import jjh.delivery.domain.returns.ProductReturn;
import jjh.delivery.domain.returns.ReturnReason;
import jjh.delivery.domain.returns.ReturnType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Return Use Case - Driving Port (Inbound)
 */
public interface ManageReturnUseCase {

    /**
     * 반품 요청
     */
    ProductReturn requestReturn(RequestReturnCommand command);

    /**
     * 반품 승인
     */
    ProductReturn approveReturn(String returnId);

    /**
     * 반품 거절
     */
    ProductReturn rejectReturn(String returnId, String reason);

    /**
     * 수거 예정 등록
     */
    ProductReturn schedulePickup(String returnId);

    /**
     * 수거 완료
     */
    ProductReturn completePickup(String returnId);

    /**
     * 검수 시작
     */
    ProductReturn startInspection(String returnId);

    /**
     * 반품 완료 (환불 처리)
     */
    ProductReturn completeReturn(String returnId);

    /**
     * 반품 취소
     */
    ProductReturn cancelReturn(String returnId);

    /**
     * 반품 조회
     */
    ProductReturn getReturn(String returnId);

    /**
     * 고객별 반품 목록 조회
     */
    List<ProductReturn> getReturnsByCustomerId(String customerId);

    /**
     * 주문별 반품 목록 조회
     */
    List<ProductReturn> getReturnsByOrderId(String orderId);

    // ==================== Commands ====================

    record RequestReturnCommand(
            String customerId,
            String orderId,
            ReturnType returnType,
            ReturnReason reason,
            String reasonDetail,
            List<ReturnItemCommand> items
    ) {
        public RequestReturnCommand {
            if (customerId == null || customerId.isBlank()) {
                throw new IllegalArgumentException("고객 ID는 필수입니다");
            }
            if (orderId == null || orderId.isBlank()) {
                throw new IllegalArgumentException("주문 ID는 필수입니다");
            }
            if (returnType == null) {
                throw new IllegalArgumentException("반품 유형은 필수입니다");
            }
            if (reason == null) {
                throw new IllegalArgumentException("반품 사유는 필수입니다");
            }
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("반품 상품은 최소 1개 이상이어야 합니다");
            }
        }
    }

    record ReturnItemCommand(
            String orderItemId,
            String productId,
            String productName,
            String variantId,
            String variantName,
            int quantity,
            BigDecimal refundAmount
    ) {
        public ReturnItemCommand {
            if (orderItemId == null || orderItemId.isBlank()) {
                throw new IllegalArgumentException("주문 항목 ID는 필수입니다");
            }
            if (productId == null || productId.isBlank()) {
                throw new IllegalArgumentException("상품 ID는 필수입니다");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("수량은 0보다 커야 합니다");
            }
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("환불 금액은 0 이상이어야 합니다");
            }
        }
    }
}

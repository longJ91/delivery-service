package jjh.delivery.domain.payment;

/**
 * Payment Status Enum
 */
public enum PaymentStatus {

    /** 결제 대기 */
    PENDING,

    /** 결제 완료 */
    COMPLETED,

    /** 결제 실패 */
    FAILED,

    /** 결제 취소 */
    CANCELLED,

    /** 부분 환불 */
    PARTIALLY_REFUNDED,

    /** 전체 환불 */
    FULLY_REFUNDED;

    /**
     * 환불 가능 여부
     */
    public boolean isRefundable() {
        return this == COMPLETED || this == PARTIALLY_REFUNDED;
    }

    /**
     * 완료 상태인지 확인
     */
    public boolean isCompleted() {
        return this == COMPLETED || this == PARTIALLY_REFUNDED;
    }
}

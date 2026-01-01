package jjh.delivery.domain.returns;

/**
 * Return Type Enum
 */
public enum ReturnType {

    /** 환불 */
    REFUND,

    /** 교환 */
    EXCHANGE;

    /**
     * 환불 타입인지 확인
     */
    public boolean isRefund() {
        return this == REFUND;
    }

    /**
     * 교환 타입인지 확인
     */
    public boolean isExchange() {
        return this == EXCHANGE;
    }
}

package jjh.delivery.domain.payment;

/**
 * Payment Method Type Enum
 */
public enum PaymentMethodType {

    /** 신용카드 */
    CREDIT_CARD,

    /** 체크카드 */
    DEBIT_CARD,

    /** 가상계좌 */
    VIRTUAL_ACCOUNT,

    /** 계좌이체 */
    BANK_TRANSFER,

    /** 휴대폰 결제 */
    MOBILE,

    /** 간편결제 (카카오페이, 네이버페이 등) */
    EASY_PAY,

    /** 포인트 */
    POINTS,

    /** 상품권 */
    GIFT_CARD;

    /**
     * 즉시 결제인지 확인
     */
    public boolean isInstant() {
        return this != VIRTUAL_ACCOUNT;
    }
}

package jjh.delivery.domain.seller;

/**
 * Seller Status Enum
 */
public enum SellerStatus {

    /** 심사 대기 */
    PENDING,

    /** 활성 상태 */
    ACTIVE,

    /** 휴면 상태 */
    DORMANT,

    /** 정지 상태 */
    SUSPENDED,

    /** 폐업 */
    CLOSED;

    /**
     * 활성 상태인지 확인
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 판매 가능한 상태인지 확인
     */
    public boolean canSell() {
        return this == ACTIVE;
    }

    /**
     * 정지 가능한 상태인지 확인
     */
    public boolean canBeSuspended() {
        return this == ACTIVE || this == DORMANT;
    }

    /**
     * 폐업 가능한 상태인지 확인
     */
    public boolean canClose() {
        return this != CLOSED && this != PENDING;
    }
}

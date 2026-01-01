package jjh.delivery.domain.customer;

/**
 * Customer Status Enum
 */
public enum CustomerStatus {

    /** 활성 상태 */
    ACTIVE,

    /** 휴면 상태 */
    DORMANT,

    /** 정지 상태 */
    SUSPENDED,

    /** 탈퇴 */
    WITHDRAWN;

    /**
     * 활성 상태인지 확인
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 정지 가능한 상태인지 확인
     */
    public boolean canBeSuspended() {
        return this == ACTIVE || this == DORMANT;
    }

    /**
     * 탈퇴 가능한 상태인지 확인
     */
    public boolean canWithdraw() {
        return this != WITHDRAWN;
    }
}

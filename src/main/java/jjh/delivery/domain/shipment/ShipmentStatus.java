package jjh.delivery.domain.shipment;

import java.util.Set;

/**
 * Shipment Status Enum with State Machine Logic
 *
 * State Transitions:
 * PENDING → PICKED_UP → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
 *                                                         ↓
 *                                         FAILED_ATTEMPT → DELIVERED
 *                                                         ↓
 *                                                     RETURNED
 */
public enum ShipmentStatus {

    /** 배송 대기 */
    PENDING(Set.of("PICKED_UP", "CANCELLED")),

    /** 집화 완료 */
    PICKED_UP(Set.of("IN_TRANSIT", "CANCELLED")),

    /** 배송중 (허브 이동) */
    IN_TRANSIT(Set.of("OUT_FOR_DELIVERY", "IN_TRANSIT")),

    /** 배달중 (최종 배송) */
    OUT_FOR_DELIVERY(Set.of("DELIVERED", "FAILED_ATTEMPT")),

    /** 배송 완료 */
    DELIVERED(Set.of()),

    /** 배송 시도 실패 */
    FAILED_ATTEMPT(Set.of("OUT_FOR_DELIVERY", "RETURNED")),

    /** 취소 */
    CANCELLED(Set.of()),

    /** 반송 */
    RETURNED(Set.of());

    private final Set<String> allowedTransitions;

    ShipmentStatus(Set<String> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransitionTo(ShipmentStatus newStatus) {
        return allowedTransitions.contains(newStatus.name());
    }

    /**
     * 배송중 상태인지 확인
     */
    public boolean isInProgress() {
        return this == PENDING || this == PICKED_UP || this == IN_TRANSIT || this == OUT_FOR_DELIVERY;
    }

    /**
     * 종료 상태인지 확인
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED || this == RETURNED;
    }

    /**
     * 취소 가능한 상태인지 확인
     */
    public boolean isCancellable() {
        return this == PENDING || this == PICKED_UP;
    }
}

package jjh.delivery.domain.returns;

import java.util.Set;

/**
 * Return Status Enum
 */
public enum ReturnStatus {

    /** 반품 요청 */
    REQUESTED(Set.of("APPROVED", "REJECTED")),

    /** 반품 승인 */
    APPROVED(Set.of("PICKUP_SCHEDULED", "CANCELLED")),

    /** 수거 예정 */
    PICKUP_SCHEDULED(Set.of("PICKED_UP", "CANCELLED")),

    /** 수거 완료 */
    PICKED_UP(Set.of("INSPECTING")),

    /** 검수중 */
    INSPECTING(Set.of("COMPLETED", "REJECTED")),

    /** 반품 완료 (환불 처리) */
    COMPLETED(Set.of()),

    /** 반품 거절 */
    REJECTED(Set.of()),

    /** 반품 취소 */
    CANCELLED(Set.of());

    private final Set<String> allowedTransitions;

    ReturnStatus(Set<String> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransitionTo(ReturnStatus newStatus) {
        return allowedTransitions.contains(newStatus.name());
    }

    /**
     * 진행중인 상태인지 확인
     */
    public boolean isInProgress() {
        return this == REQUESTED || this == APPROVED || this == PICKUP_SCHEDULED
                || this == PICKED_UP || this == INSPECTING;
    }

    /**
     * 종료 상태인지 확인
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED || this == CANCELLED;
    }

    /**
     * 취소 가능한 상태인지 확인
     */
    public boolean isCancellable() {
        return this == REQUESTED || this == APPROVED || this == PICKUP_SCHEDULED;
    }
}

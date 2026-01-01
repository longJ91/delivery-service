package jjh.delivery.domain.order;

import java.util.Set;

/**
 * Order Status with State Machine Logic (v2 - Product Delivery)
 *
 * State Transitions:
 * PENDING → PAID → CONFIRMED → PREPARING → SHIPPED → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
 *                                                                                    ↓
 *                                                        CANCELLED ← RETURN_REQUESTED → RETURNED
 */
public enum OrderStatus {

    /** 주문 대기 (결제 전) */
    PENDING(Set.of("PAID", "CANCELLED")),

    /** 결제 완료 */
    PAID(Set.of("CONFIRMED", "CANCELLED")),

    /** 주문 확정 (판매자 확인) */
    CONFIRMED(Set.of("PREPARING", "CANCELLED")),

    /** 상품 준비중 */
    PREPARING(Set.of("SHIPPED", "CANCELLED")),

    /** 출고 완료 */
    SHIPPED(Set.of("IN_TRANSIT", "CANCELLED")),

    /** 배송중 (허브 이동) */
    IN_TRANSIT(Set.of("OUT_FOR_DELIVERY")),

    /** 배달중 (최종 배송) */
    OUT_FOR_DELIVERY(Set.of("DELIVERED")),

    /** 배송 완료 */
    DELIVERED(Set.of("RETURN_REQUESTED")),

    /** 취소 */
    CANCELLED(Set.of()),

    /** 반품 요청 */
    RETURN_REQUESTED(Set.of("RETURNED", "DELIVERED")),

    /** 반품 완료 */
    RETURNED(Set.of());

    private final Set<String> allowedTransitions;

    OrderStatus(Set<String> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return allowedTransitions.contains(newStatus.name());
    }

    /**
     * 취소 가능한 상태인지 확인
     */
    public boolean isCancellable() {
        return this == PENDING || this == PAID || this == CONFIRMED
                || this == PREPARING || this == SHIPPED;
    }

    /**
     * 반품 가능한 상태인지 확인
     */
    public boolean isReturnable() {
        return this == DELIVERED;
    }

    /**
     * 종료 상태인지 확인 (더 이상 상태 변경 불가)
     */
    public boolean isTerminal() {
        return this == CANCELLED || this == RETURNED;
    }

    /**
     * 배송 진행중 상태인지 확인
     */
    public boolean isInDelivery() {
        return this == SHIPPED || this == IN_TRANSIT || this == OUT_FOR_DELIVERY;
    }
}

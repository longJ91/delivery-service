package jjh.delivery.domain.order;

import java.util.Set;

/**
 * Order Status with State Machine Logic
 */
public enum OrderStatus {

    PENDING(Set.of("ACCEPTED", "CANCELLED")),
    ACCEPTED(Set.of("PREPARING", "CANCELLED")),
    PREPARING(Set.of("READY_FOR_DELIVERY", "CANCELLED")),
    READY_FOR_DELIVERY(Set.of("PICKED_UP", "CANCELLED")),
    PICKED_UP(Set.of("DELIVERED")),
    DELIVERED(Set.of()),
    CANCELLED(Set.of());

    private final Set<String> allowedTransitions;

    OrderStatus(Set<String> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return allowedTransitions.contains(newStatus.name());
    }
}

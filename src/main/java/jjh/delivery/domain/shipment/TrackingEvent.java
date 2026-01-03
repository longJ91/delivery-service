package jjh.delivery.domain.shipment;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracking Event Value Object
 * 배송 추적 이벤트
 */
public record TrackingEvent(
        UUID id,
        ShipmentStatus status,
        String location,
        String description,
        LocalDateTime occurredAt,
        LocalDateTime createdAt
) {
    public TrackingEvent {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Factory method
     */
    public static TrackingEvent of(ShipmentStatus status, String location, String description) {
        return new TrackingEvent(
                UUID.randomUUID(),
                status,
                location,
                description,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /**
     * Factory method with occurred time
     */
    public static TrackingEvent of(ShipmentStatus status, String location, String description, LocalDateTime occurredAt) {
        return new TrackingEvent(
                UUID.randomUUID(),
                status,
                location,
                description,
                occurredAt,
                LocalDateTime.now()
        );
    }
}

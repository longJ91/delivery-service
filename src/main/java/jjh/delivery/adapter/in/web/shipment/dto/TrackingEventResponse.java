package jjh.delivery.adapter.in.web.shipment.dto;

import jjh.delivery.domain.shipment.ShipmentStatus;
import jjh.delivery.domain.shipment.TrackingEvent;

import java.time.LocalDateTime;

/**
 * 추적 이벤트 응답
 */
public record TrackingEventResponse(
        String id,
        ShipmentStatus status,
        String location,
        String description,
        LocalDateTime occurredAt
) {
    public static TrackingEventResponse from(TrackingEvent event) {
        return new TrackingEventResponse(
                event.id(),
                event.status(),
                event.location(),
                event.description(),
                event.occurredAt()
        );
    }
}

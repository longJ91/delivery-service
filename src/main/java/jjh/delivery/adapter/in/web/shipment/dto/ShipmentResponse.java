package jjh.delivery.adapter.in.web.shipment.dto;

import jjh.delivery.domain.shipment.Shipment;
import jjh.delivery.domain.shipment.ShipmentStatus;
import jjh.delivery.domain.shipment.ShippingCarrier;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 배송 응답
 */
public record ShipmentResponse(
        String id,
        String orderId,
        ShippingCarrier carrier,
        String carrierDisplayName,
        String trackingNumber,
        String trackingUrl,
        ShipmentStatus status,
        boolean isDelivered,
        boolean isInProgress,
        List<TrackingEventResponse> trackingEvents,
        LocalDateTime estimatedDeliveryDate,
        LocalDateTime createdAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt
) {
    public static ShipmentResponse from(Shipment shipment) {
        List<TrackingEventResponse> events = shipment.getTrackingEvents().stream()
                .map(TrackingEventResponse::from)
                .toList();

        return new ShipmentResponse(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getCarrier(),
                shipment.getCarrier() != null ? shipment.getCarrier().getDisplayName() : null,
                shipment.getTrackingNumber(),
                shipment.getTrackingUrl(),
                shipment.getStatus(),
                shipment.isDelivered(),
                shipment.isInProgress(),
                events,
                shipment.getEstimatedDeliveryDate(),
                shipment.getCreatedAt(),
                shipment.getShippedAt(),
                shipment.getDeliveredAt()
        );
    }
}

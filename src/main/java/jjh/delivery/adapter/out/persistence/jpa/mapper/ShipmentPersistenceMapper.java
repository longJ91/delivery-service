package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.ShipmentJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.ShipmentTrackingJpaEntity;
import jjh.delivery.domain.shipment.Shipment;
import jjh.delivery.domain.shipment.TrackingEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Shipment Persistence Mapper
 * Entity <-> Domain 변환
 */
@Component
public class ShipmentPersistenceMapper {

    public Shipment toDomain(ShipmentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<TrackingEvent> trackingEvents = entity.getTrackingEvents().stream()
                .map(this::toTrackingEventDomain)
                .toList();

        return Shipment.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .carrier(entity.getCarrier())
                .trackingNumber(entity.getTrackingNumber())
                .status(entity.getStatus())
                .trackingEvents(trackingEvents)
                .estimatedDeliveryDate(entity.getEstimatedDeliveryDate())
                .createdAt(entity.getCreatedAt())
                .shippedAt(entity.getShippedAt())
                .deliveredAt(entity.getDeliveredAt())
                .build();
    }

    public ShipmentJpaEntity toEntity(Shipment domain) {
        if (domain == null) {
            return null;
        }

        ShipmentJpaEntity entity = new ShipmentJpaEntity(
                domain.getId(),
                domain.getOrderId(),
                domain.getCarrier(),
                domain.getTrackingNumber(),
                domain.getStatus(),
                domain.getEstimatedDeliveryDate(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getShippedAt(),
                domain.getDeliveredAt()
        );

        // Add tracking events
        entity.clearTrackingEvents();
        for (TrackingEvent event : domain.getTrackingEvents()) {
            ShipmentTrackingJpaEntity trackingEntity = toTrackingEventEntity(event);
            entity.addTrackingEvent(trackingEntity);
        }

        return entity;
    }

    private TrackingEvent toTrackingEventDomain(ShipmentTrackingJpaEntity entity) {
        return new TrackingEvent(
                entity.getId(),
                entity.getStatus(),
                entity.getLocation(),
                entity.getDescription(),
                entity.getOccurredAt(),
                entity.getCreatedAt()
        );
    }

    private ShipmentTrackingJpaEntity toTrackingEventEntity(TrackingEvent domain) {
        return new ShipmentTrackingJpaEntity(
                domain.id(),
                domain.status(),
                domain.location(),
                domain.description(),
                domain.occurredAt(),
                domain.createdAt()
        );
    }
}

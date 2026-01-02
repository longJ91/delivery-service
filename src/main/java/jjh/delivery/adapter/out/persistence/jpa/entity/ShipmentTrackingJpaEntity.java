package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import jjh.delivery.domain.shipment.ShipmentStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Shipment Tracking Event JPA Entity
 */
@Entity
@Table(name = "shipment_tracking", indexes = {
        @Index(name = "idx_shipment_tracking_shipment_id", columnList = "shipment_id"),
        @Index(name = "idx_shipment_tracking_occurred_at", columnList = "occurred_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentTrackingJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private ShipmentJpaEntity shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(length = 200)
    private String location;

    @Column(length = 500)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ShipmentTrackingJpaEntity(
            String id,
            ShipmentStatus status,
            String location,
            String description,
            LocalDateTime occurredAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.status = status;
        this.location = location;
        this.description = description;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
    }

    void setShipment(ShipmentJpaEntity shipment) {
        this.shipment = shipment;
    }
}

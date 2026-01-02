package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import jjh.delivery.domain.shipment.ShipmentStatus;
import jjh.delivery.domain.shipment.ShippingCarrier;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shipment JPA Entity
 */
@Entity
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipments_order_id", columnList = "order_id"),
        @Index(name = "idx_shipments_tracking_number", columnList = "tracking_number"),
        @Index(name = "idx_shipments_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ShippingCarrier carrier;

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt ASC")
    private List<ShipmentTrackingJpaEntity> trackingEvents = new ArrayList<>();

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Version
    private Long version;

    @Builder
    public ShipmentJpaEntity(
            String id,
            String orderId,
            ShippingCarrier carrier,
            String trackingNumber,
            ShipmentStatus status,
            LocalDateTime estimatedDeliveryDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt
    ) {
        this.id = id;
        this.orderId = orderId;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
    }

    public void addTrackingEvent(ShipmentTrackingJpaEntity event) {
        trackingEvents.add(event);
        event.setShipment(this);
    }

    public void clearTrackingEvents() {
        trackingEvents.clear();
    }

    // Setters for update
    public void setCarrier(ShippingCarrier carrier) {
        this.carrier = carrier;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}

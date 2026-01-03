package jjh.delivery.application.port.out;

import jjh.delivery.domain.shipment.Shipment;
import jjh.delivery.domain.shipment.ShipmentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Shipment Load Port - Driven Port (Outbound)
 */
public interface LoadShipmentPort {

    Optional<Shipment> findById(UUID shipmentId);

    Optional<Shipment> findByOrderId(UUID orderId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByStatus(ShipmentStatus status);

    boolean existsByOrderId(UUID orderId);
}

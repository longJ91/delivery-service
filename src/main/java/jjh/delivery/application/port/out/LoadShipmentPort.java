package jjh.delivery.application.port.out;

import jjh.delivery.domain.shipment.Shipment;
import jjh.delivery.domain.shipment.ShipmentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Shipment Load Port - Driven Port (Outbound)
 */
public interface LoadShipmentPort {

    Optional<Shipment> findById(String shipmentId);

    Optional<Shipment> findByOrderId(String orderId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByStatus(ShipmentStatus status);

    boolean existsByOrderId(String orderId);
}

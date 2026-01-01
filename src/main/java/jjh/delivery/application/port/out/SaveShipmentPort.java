package jjh.delivery.application.port.out;

import jjh.delivery.domain.shipment.Shipment;

/**
 * Shipment Save Port - Driven Port (Outbound)
 */
public interface SaveShipmentPort {

    Shipment save(Shipment shipment);
}

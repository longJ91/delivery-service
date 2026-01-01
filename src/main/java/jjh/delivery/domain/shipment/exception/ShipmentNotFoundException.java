package jjh.delivery.domain.shipment.exception;

/**
 * Exception thrown when shipment is not found
 */
public class ShipmentNotFoundException extends RuntimeException {

    private final String shipmentId;

    public ShipmentNotFoundException(String shipmentId) {
        super("Shipment not found: " + shipmentId);
        this.shipmentId = shipmentId;
    }

    public String getShipmentId() {
        return shipmentId;
    }
}

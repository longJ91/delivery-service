package jjh.delivery.application.service;

import jjh.delivery.application.port.in.ManageShipmentUseCase;
import jjh.delivery.application.port.out.LoadShipmentPort;
import jjh.delivery.application.port.out.SaveShipmentPort;
import jjh.delivery.domain.shipment.Shipment;
import jjh.delivery.domain.shipment.ShipmentStatus;
import jjh.delivery.domain.shipment.exception.ShipmentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shipment Service - Application Layer
 */
@Service
@Transactional
public class ShipmentService implements ManageShipmentUseCase {

    private final LoadShipmentPort loadShipmentPort;
    private final SaveShipmentPort saveShipmentPort;

    public ShipmentService(
            LoadShipmentPort loadShipmentPort,
            SaveShipmentPort saveShipmentPort
    ) {
        this.loadShipmentPort = loadShipmentPort;
        this.saveShipmentPort = saveShipmentPort;
    }

    @Override
    public Shipment createShipment(CreateShipmentCommand command) {
        if (loadShipmentPort.existsByOrderId(command.orderId())) {
            throw new IllegalStateException("이미 해당 주문에 대한 배송이 존재합니다: " + command.orderId());
        }

        Shipment shipment = Shipment.builder()
                .orderId(command.orderId())
                .estimatedDeliveryDate(command.estimatedDeliveryDate())
                .build();

        return saveShipmentPort.save(shipment);
    }

    @Override
    public Shipment registerTracking(RegisterTrackingCommand command) {
        Shipment shipment = loadShipmentPort.findById(command.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(command.shipmentId()));

        shipment.registerTracking(command.carrier(), command.trackingNumber());

        return saveShipmentPort.save(shipment);
    }

    @Override
    public Shipment updateStatus(UpdateShipmentStatusCommand command) {
        Shipment shipment = loadShipmentPort.findById(command.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(command.shipmentId()));

        ShipmentStatus status = command.status();
        String location = command.location();
        String description = command.description();

        switch (status) {
            case PICKED_UP -> shipment.pickUp(location);
            case IN_TRANSIT -> shipment.inTransit(location, description);
            case OUT_FOR_DELIVERY -> shipment.outForDelivery(location);
            case DELIVERED -> shipment.deliver(location);
            case FAILED_ATTEMPT -> shipment.failDeliveryAttempt(location, description);
            case CANCELLED -> shipment.cancel(description);
            case RETURNED -> shipment.returnToSender(location, description);
            default -> throw new IllegalArgumentException("지원하지 않는 상태 변경입니다: " + status);
        }

        return saveShipmentPort.save(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Shipment getShipment(String shipmentId) {
        return loadShipmentPort.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Shipment getShipmentByOrderId(String orderId) {
        return loadShipmentPort.findByOrderId(orderId)
                .orElseThrow(() -> new ShipmentNotFoundException("주문에 대한 배송을 찾을 수 없습니다: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Shipment getShipmentByTrackingNumber(String trackingNumber) {
        return loadShipmentPort.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("운송장 번호로 배송을 찾을 수 없습니다: " + trackingNumber));
    }
}

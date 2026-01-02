package jjh.delivery.adapter.in.web.shipment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.shipment.dto.*;
import jjh.delivery.application.port.in.ManageShipmentUseCase;
import jjh.delivery.application.port.in.ManageShipmentUseCase.*;
import jjh.delivery.domain.shipment.Shipment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Shipment REST Controller - Driving Adapter (Inbound)
 * 배송 관리 API
 */
@RestController
@RequestMapping("/api/v2/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ManageShipmentUseCase manageShipmentUseCase;

    /**
     * 배송 생성
     */
    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody CreateShipmentRequest request
    ) {
        CreateShipmentCommand command = new CreateShipmentCommand(
                request.orderId(),
                request.estimatedDeliveryDate()
        );

        Shipment shipment = manageShipmentUseCase.createShipment(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(ShipmentResponse.from(shipment));
    }

    /**
     * 운송장 등록
     */
    @PostMapping("/{shipmentId}/tracking")
    public ResponseEntity<ShipmentResponse> registerTracking(
            @PathVariable String shipmentId,
            @Valid @RequestBody RegisterTrackingRequest request
    ) {
        RegisterTrackingCommand command = new RegisterTrackingCommand(
                shipmentId,
                request.carrier(),
                request.trackingNumber()
        );

        Shipment shipment = manageShipmentUseCase.registerTracking(command);

        return ResponseEntity.ok(ShipmentResponse.from(shipment));
    }

    /**
     * 배송 상태 업데이트
     */
    @PatchMapping("/{shipmentId}/status")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable String shipmentId,
            @Valid @RequestBody UpdateShipmentStatusRequest request
    ) {
        UpdateShipmentStatusCommand command = new UpdateShipmentStatusCommand(
                shipmentId,
                request.status(),
                request.location(),
                request.description()
        );

        Shipment shipment = manageShipmentUseCase.updateStatus(command);

        return ResponseEntity.ok(ShipmentResponse.from(shipment));
    }

    /**
     * 배송 조회
     */
    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> getShipment(
            @PathVariable String shipmentId
    ) {
        Shipment shipment = manageShipmentUseCase.getShipment(shipmentId);

        return ResponseEntity.ok(ShipmentResponse.from(shipment));
    }

    /**
     * 주문별 배송 조회
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentResponse> getShipmentByOrderId(
            @PathVariable String orderId
    ) {
        Shipment shipment = manageShipmentUseCase.getShipmentByOrderId(orderId);

        return ResponseEntity.ok(ShipmentResponse.from(shipment));
    }

    /**
     * 운송장 번호로 조회
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getShipmentByTrackingNumber(
            @PathVariable String trackingNumber
    ) {
        Shipment shipment = manageShipmentUseCase.getShipmentByTrackingNumber(trackingNumber);

        return ResponseEntity.ok(ShipmentResponse.from(shipment));
    }
}

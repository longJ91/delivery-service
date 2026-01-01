package jjh.delivery.application.port.in;

import jjh.delivery.domain.shipment.Shipment;
import jjh.delivery.domain.shipment.ShipmentStatus;
import jjh.delivery.domain.shipment.ShippingCarrier;

import java.time.LocalDateTime;

/**
 * Shipment Use Case - Driving Port (Inbound)
 */
public interface ManageShipmentUseCase {

    /**
     * 배송 생성
     */
    Shipment createShipment(CreateShipmentCommand command);

    /**
     * 운송장 등록
     */
    Shipment registerTracking(RegisterTrackingCommand command);

    /**
     * 배송 상태 업데이트
     */
    Shipment updateStatus(UpdateShipmentStatusCommand command);

    /**
     * 배송 조회
     */
    Shipment getShipment(String shipmentId);

    /**
     * 주문별 배송 조회
     */
    Shipment getShipmentByOrderId(String orderId);

    /**
     * 운송장 번호로 조회
     */
    Shipment getShipmentByTrackingNumber(String trackingNumber);

    // ==================== Commands ====================

    record CreateShipmentCommand(
            String orderId,
            LocalDateTime estimatedDeliveryDate
    ) {
        public CreateShipmentCommand {
            if (orderId == null || orderId.isBlank()) {
                throw new IllegalArgumentException("주문 ID는 필수입니다");
            }
        }
    }

    record RegisterTrackingCommand(
            String shipmentId,
            ShippingCarrier carrier,
            String trackingNumber
    ) {
        public RegisterTrackingCommand {
            if (shipmentId == null || shipmentId.isBlank()) {
                throw new IllegalArgumentException("배송 ID는 필수입니다");
            }
            if (carrier == null) {
                throw new IllegalArgumentException("택배사는 필수입니다");
            }
            if (trackingNumber == null || trackingNumber.isBlank()) {
                throw new IllegalArgumentException("운송장 번호는 필수입니다");
            }
        }
    }

    record UpdateShipmentStatusCommand(
            String shipmentId,
            ShipmentStatus status,
            String location,
            String description
    ) {
        public UpdateShipmentStatusCommand {
            if (shipmentId == null || shipmentId.isBlank()) {
                throw new IllegalArgumentException("배송 ID는 필수입니다");
            }
            if (status == null) {
                throw new IllegalArgumentException("배송 상태는 필수입니다");
            }
        }
    }
}

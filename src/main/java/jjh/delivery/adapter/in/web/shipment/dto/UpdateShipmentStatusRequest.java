package jjh.delivery.adapter.in.web.shipment.dto;

import jakarta.validation.constraints.NotNull;
import jjh.delivery.domain.shipment.ShipmentStatus;

/**
 * 배송 상태 업데이트 요청
 */
public record UpdateShipmentStatusRequest(

        @NotNull(message = "배송 상태는 필수입니다")
        ShipmentStatus status,

        String location,

        String description

) {}

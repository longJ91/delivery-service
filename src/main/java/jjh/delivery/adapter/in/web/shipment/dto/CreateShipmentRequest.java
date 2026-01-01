package jjh.delivery.adapter.in.web.shipment.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * 배송 생성 요청
 */
public record CreateShipmentRequest(

        @NotBlank(message = "주문 ID는 필수입니다")
        String orderId,

        LocalDateTime estimatedDeliveryDate

) {}

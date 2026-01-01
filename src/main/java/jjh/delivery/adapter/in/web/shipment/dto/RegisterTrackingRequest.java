package jjh.delivery.adapter.in.web.shipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jjh.delivery.domain.shipment.ShippingCarrier;

/**
 * 운송장 등록 요청
 */
public record RegisterTrackingRequest(

        @NotNull(message = "택배사는 필수입니다")
        ShippingCarrier carrier,

        @NotBlank(message = "운송장 번호는 필수입니다")
        String trackingNumber

) {}

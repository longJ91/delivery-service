package jjh.delivery.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jjh.delivery.domain.order.OrderStatus;

/**
 * Update Order Status Request DTO
 */
public record UpdateOrderStatusRequest(

        @NotNull(message = "주문 상태는 필수입니다")
        OrderStatus status

) {}

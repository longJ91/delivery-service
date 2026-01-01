package jjh.delivery.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Create Order Request DTO
 * 형식 검증 (Format Validation) 담당
 */
public record CreateOrderRequest(

        @NotBlank(message = "고객 ID는 필수입니다")
        String customerId,

        @NotBlank(message = "가게 ID는 필수입니다")
        String shopId,

        @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
        @Valid
        List<OrderItemRequest> items,

        @NotBlank(message = "배송 주소는 필수입니다")
        @Size(max = 500, message = "배송 주소는 500자 이하여야 합니다")
        String deliveryAddress

) {
    public record OrderItemRequest(

            @NotBlank(message = "메뉴 ID는 필수입니다")
            String menuId,

            @NotBlank(message = "메뉴명은 필수입니다")
            String menuName,

            @Positive(message = "수량은 1 이상이어야 합니다")
            int quantity,

            @NotNull(message = "단가는 필수입니다")
            @Positive(message = "단가는 0보다 커야 합니다")
            BigDecimal unitPrice

    ) {}
}

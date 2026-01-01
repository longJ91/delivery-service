package jjh.delivery.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Create Order Request DTO (v2 - Product Delivery)
 * 형식 검증 (Format Validation) 담당
 */
public record CreateOrderRequest(

        @NotBlank(message = "고객 ID는 필수입니다")
        String customerId,

        @NotBlank(message = "판매자 ID는 필수입니다")
        String sellerId,

        @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
        @Valid
        List<OrderItemRequest> items,

        @NotNull(message = "배송 주소는 필수입니다")
        @Valid
        ShippingAddressRequest shippingAddress,

        @Size(max = 500, message = "주문 메모는 500자 이하여야 합니다")
        String orderMemo,

        @Size(max = 500, message = "배송 메모는 500자 이하여야 합니다")
        String shippingMemo,

        String couponId

) {
    public record OrderItemRequest(

            @NotBlank(message = "상품 ID는 필수입니다")
            String productId,

            @NotBlank(message = "상품명은 필수입니다")
            String productName,

            String variantId,

            String variantName,

            String sku,

            Map<String, String> optionValues,

            @Positive(message = "수량은 1 이상이어야 합니다")
            int quantity,

            @NotNull(message = "단가는 필수입니다")
            @Positive(message = "단가는 0보다 커야 합니다")
            BigDecimal unitPrice

    ) {}

    public record ShippingAddressRequest(

            @NotBlank(message = "수령인 이름은 필수입니다")
            String recipientName,

            @NotBlank(message = "전화번호는 필수입니다")
            String phoneNumber,

            @NotBlank(message = "우편번호는 필수입니다")
            String postalCode,

            @NotBlank(message = "기본 주소는 필수입니다")
            String address1,

            String address2,

            @Size(max = 200, message = "배송 메모는 200자 이하여야 합니다")
            String deliveryNote

    ) {}
}

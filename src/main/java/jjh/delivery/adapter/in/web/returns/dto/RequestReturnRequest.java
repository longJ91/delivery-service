package jjh.delivery.adapter.in.web.returns.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jjh.delivery.domain.returns.ReturnReason;
import jjh.delivery.domain.returns.ReturnType;

import java.util.List;

/**
 * 반품 요청
 */
public record RequestReturnRequest(

        @NotBlank(message = "주문 ID는 필수입니다")
        String orderId,

        @NotNull(message = "반품 유형은 필수입니다")
        ReturnType returnType,

        @NotNull(message = "반품 사유는 필수입니다")
        ReturnReason reason,

        String reasonDetail,

        @NotEmpty(message = "반품 상품은 최소 1개 이상이어야 합니다")
        @Valid
        List<ReturnItemRequest> items

) {}

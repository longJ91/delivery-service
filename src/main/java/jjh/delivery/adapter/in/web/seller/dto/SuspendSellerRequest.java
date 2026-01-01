package jjh.delivery.adapter.in.web.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 판매자 정지 요청
 */
public record SuspendSellerRequest(
        @NotBlank(message = "정지 사유는 필수입니다")
        @Size(max = 500, message = "정지 사유는 500자 이내여야 합니다")
        String reason
) {
}

package jjh.delivery.adapter.in.web.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 판매자 거절 요청
 */
public record RejectSellerRequest(
        @NotBlank(message = "거절 사유는 필수입니다")
        @Size(max = 500, message = "거절 사유는 500자 이내여야 합니다")
        String reason
) {
}

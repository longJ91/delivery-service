package jjh.delivery.adapter.in.web.returns.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 반품 거절 요청
 */
public record RejectReturnRequest(

        @NotBlank(message = "거절 사유는 필수입니다")
        String reason

) {}

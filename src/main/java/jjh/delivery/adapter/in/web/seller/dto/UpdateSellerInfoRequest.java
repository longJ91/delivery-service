package jjh.delivery.adapter.in.web.seller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 판매자 정보 수정 요청
 */
public record UpdateSellerInfoRequest(
        @Size(max = 200, message = "상호명은 200자 이내여야 합니다")
        String businessName,

        @Size(max = 100, message = "대표자명은 100자 이내여야 합니다")
        String representativeName,

        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @Size(max = 20, message = "전화번호는 20자 이내여야 합니다")
        String phoneNumber
) {
}

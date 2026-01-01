package jjh.delivery.adapter.in.web.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 배송지 등록 요청
 */
public record AddAddressRequest(

        @NotBlank(message = "배송지 이름은 필수입니다")
        @Size(max = 50, message = "배송지 이름은 50자 이하여야 합니다")
        String name,

        @NotBlank(message = "수령인 이름은 필수입니다")
        @Size(max = 100, message = "수령인 이름은 100자 이하여야 합니다")
        String recipientName,

        @NotBlank(message = "전화번호는 필수입니다")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
        String recipientPhone,

        @NotBlank(message = "우편번호는 필수입니다")
        @Size(max = 10, message = "우편번호는 10자 이하여야 합니다")
        String postalCode,

        @NotBlank(message = "도로명 주소는 필수입니다")
        @Size(max = 200, message = "도로명 주소는 200자 이하여야 합니다")
        String roadAddress,

        @Size(max = 200, message = "상세 주소는 200자 이하여야 합니다")
        String detailAddress,

        boolean isDefault

) {}

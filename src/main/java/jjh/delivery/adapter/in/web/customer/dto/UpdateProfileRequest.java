package jjh.delivery.adapter.in.web.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 프로필 수정 요청
 */
public record UpdateProfileRequest(

        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 100, message = "이름은 100자 이하여야 합니다")
        String name,

        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
        String phone

) {}

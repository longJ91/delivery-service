package jjh.delivery.adapter.in.web.seller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jjh.delivery.domain.seller.SellerType;

import java.util.List;

/**
 * 판매자 등록 요청
 */
public record RegisterSellerRequest(
        @NotBlank(message = "상호명은 필수입니다")
        @Size(max = 200, message = "상호명은 200자 이내여야 합니다")
        String businessName,

        @NotBlank(message = "사업자번호는 필수입니다")
        @Size(max = 20, message = "사업자번호는 20자 이내여야 합니다")
        String businessNumber,

        @NotBlank(message = "대표자명은 필수입니다")
        @Size(max = 100, message = "대표자명은 100자 이내여야 합니다")
        String representativeName,

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @Size(max = 20, message = "전화번호는 20자 이내여야 합니다")
        String phoneNumber,

        SellerType sellerType,

        @Valid
        WarehouseAddressRequest warehouseAddress,

        List<String> categoryIds
) {
}

package jjh.delivery.adapter.in.web.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jjh.delivery.application.port.in.ManageSellerUseCase.WarehouseAddressCommand;

/**
 * 창고 주소 요청
 */
public record WarehouseAddressRequest(
        @NotBlank(message = "우편번호는 필수입니다")
        @Size(max = 10, message = "우편번호는 10자 이내여야 합니다")
        String postalCode,

        @NotBlank(message = "기본주소는 필수입니다")
        @Size(max = 200, message = "기본주소는 200자 이내여야 합니다")
        String address1,

        @Size(max = 200, message = "상세주소는 200자 이내여야 합니다")
        String address2,

        @NotBlank(message = "담당자명은 필수입니다")
        @Size(max = 100, message = "담당자명은 100자 이내여야 합니다")
        String contactName,

        @NotBlank(message = "담당자 연락처는 필수입니다")
        @Size(max = 20, message = "담당자 연락처는 20자 이내여야 합니다")
        String contactPhone
) {
    public WarehouseAddressCommand toCommand() {
        return new WarehouseAddressCommand(postalCode, address1, address2, contactName, contactPhone);
    }
}

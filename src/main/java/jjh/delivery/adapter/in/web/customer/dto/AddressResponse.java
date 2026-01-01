package jjh.delivery.adapter.in.web.customer.dto;

import jjh.delivery.domain.customer.CustomerAddress;

import java.util.List;

/**
 * 배송지 응답
 */
public record AddressResponse(
        String id,
        String name,
        String recipientName,
        String recipientPhone,
        String postalCode,
        String roadAddress,
        String detailAddress,
        boolean isDefault
) {
    public static AddressResponse from(CustomerAddress address) {
        return new AddressResponse(
                address.id(),
                address.name(),
                address.recipientName(),
                address.phoneNumber(),
                address.postalCode(),
                address.address1(),
                address.address2(),
                address.isDefault()
        );
    }

    public static List<AddressResponse> fromList(List<CustomerAddress> addresses) {
        return addresses.stream()
                .map(AddressResponse::from)
                .toList();
    }
}

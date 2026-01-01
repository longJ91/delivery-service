package jjh.delivery.adapter.in.web.customer.dto;

import jjh.delivery.domain.customer.CustomerAddress;

import java.util.List;

/**
 * 배송지 목록 응답
 */
public record AddressListResponse(
        List<AddressResponse> addresses
) {
    public static AddressListResponse from(List<CustomerAddress> addresses) {
        return new AddressListResponse(
                AddressResponse.fromList(addresses)
        );
    }
}

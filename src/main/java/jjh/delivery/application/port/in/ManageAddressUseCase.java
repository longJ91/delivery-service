package jjh.delivery.application.port.in;

import jjh.delivery.domain.customer.Customer;
import jjh.delivery.domain.customer.CustomerAddress;

import java.util.List;

/**
 * Manage Address Use Case - Driving Port (Inbound)
 * 배송지 관리 (조회, 추가, 삭제, 기본 설정)
 */
public interface ManageAddressUseCase {

    List<CustomerAddress> getAddresses(String customerId);

    CustomerAddress addAddress(String customerId, AddAddressCommand command);

    void removeAddress(String customerId, String addressId);

    void setDefaultAddress(String customerId, String addressId);

    record AddAddressCommand(
            String name,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String address1,
            String address2,
            boolean isDefault
    ) {
        public AddAddressCommand {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("배송지 이름은 필수입니다");
            }
            if (recipientName == null || recipientName.isBlank()) {
                throw new IllegalArgumentException("수령인 이름은 필수입니다");
            }
            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new IllegalArgumentException("전화번호는 필수입니다");
            }
            if (postalCode == null || postalCode.isBlank()) {
                throw new IllegalArgumentException("우편번호는 필수입니다");
            }
            if (address1 == null || address1.isBlank()) {
                throw new IllegalArgumentException("기본 주소는 필수입니다");
            }
        }
    }
}

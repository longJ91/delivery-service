package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;

import jjh.delivery.application.port.in.ManageAddressUseCase;
import jjh.delivery.application.port.in.UpdateCustomerProfileUseCase;
import jjh.delivery.application.port.out.LoadCustomerPort;
import jjh.delivery.application.port.out.SaveCustomerPort;
import jjh.delivery.domain.customer.Customer;
import jjh.delivery.domain.customer.CustomerAddress;
import jjh.delivery.domain.customer.exception.CustomerNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Customer Service
 * 고객 프로필 및 배송지 관리
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService implements UpdateCustomerProfileUseCase, ManageAddressUseCase {

    private final LoadCustomerPort loadCustomerPort;
    private final SaveCustomerPort saveCustomerPort;

    // ==================== UpdateCustomerProfileUseCase ====================

    @Override
    public Customer updateProfile(String customerId, UpdateProfileCommand command) {
        Customer customer = getCustomerOrThrow(customerId);
        customer.updateProfile(command.name(), command.phoneNumber());
        return saveCustomerPort.save(customer);
    }

    // ==================== ManageAddressUseCase ====================

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddress> getAddresses(String customerId) {
        Customer customer = getCustomerOrThrow(customerId);
        return customer.getAddresses();
    }

    @Override
    public CustomerAddress addAddress(String customerId, AddAddressCommand command) {
        Customer customer = getCustomerOrThrow(customerId);

        CustomerAddress newAddress = CustomerAddress.of(
                command.name(),
                command.recipientName(),
                command.phoneNumber(),
                command.postalCode(),
                command.address1(),
                command.address2(),
                command.isDefault()
        );

        customer.addAddress(newAddress);
        Customer saved = saveCustomerPort.save(customer);

        // 새로 추가된 주소 반환 (ID로 찾기)
        return saved.getAddresses().stream()
                .filter(addr -> addr.name().equals(command.name())
                        && addr.recipientName().equals(command.recipientName()))
                .reduce((first, second) -> second) // 가장 최근 것
                .orElse(newAddress);
    }

    @Override
    public void removeAddress(String customerId, String addressId) {
        Customer customer = getCustomerOrThrow(customerId);
        customer.removeAddress(addressId);
        saveCustomerPort.save(customer);
    }

    @Override
    public void setDefaultAddress(String customerId, String addressId) {
        Customer customer = getCustomerOrThrow(customerId);
        customer.setDefaultAddress(addressId);
        saveCustomerPort.save(customer);
    }

    // ==================== Private Methods ====================

    private Customer getCustomerOrThrow(String customerId) {
        return loadCustomerPort.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
}

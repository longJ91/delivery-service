package jjh.delivery.application.port.out;

import jjh.delivery.domain.customer.Customer;

import java.util.UUID;

/**
 * Save Customer Port - Driven Port (Outbound)
 * 고객 저장을 위한 포트
 * Note: updatePassword는 UpdateCustomerCredentialsPort로 분리됨
 */
public interface SaveCustomerPort {

    Customer save(Customer customer);

    Customer saveWithPassword(Customer customer, String encodedPassword);

    void delete(UUID customerId);
}

package jjh.delivery.application.port.out;

import jjh.delivery.domain.customer.Customer;

/**
 * Save Customer Port - Driven Port (Outbound)
 * 고객 저장을 위한 포트
 */
public interface SaveCustomerPort {

    Customer save(Customer customer);

    Customer saveWithPassword(Customer customer, String encodedPassword);

    void updatePassword(String customerId, String encodedPassword);

    void delete(String customerId);
}

package jjh.delivery.application.port.out;

import jjh.delivery.domain.customer.Customer;

import java.util.Optional;
import java.util.UUID;

/**
 * Load Customer Port - Driven Port (Outbound)
 * 고객 조회를 위한 포트
 */
public interface LoadCustomerPort {

    Optional<Customer> findById(UUID customerId);

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsById(UUID customerId);
}

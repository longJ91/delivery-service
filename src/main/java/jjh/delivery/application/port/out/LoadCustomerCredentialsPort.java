package jjh.delivery.application.port.out;

import java.util.Optional;

/**
 * Load Customer Credentials Port - Driven Port (Outbound)
 * 고객 인증 정보 조회를 위한 포트
 */
public interface LoadCustomerCredentialsPort {

    Optional<String> findPasswordByEmail(String email);

    Optional<String> findPasswordByCustomerId(String customerId);
}

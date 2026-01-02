package jjh.delivery.adapter.out.persistence.jooq;

import jjh.delivery.adapter.out.persistence.jooq.repository.CustomerJooqRepository;
import jjh.delivery.application.port.out.LoadCustomerCredentialsPort;
import jjh.delivery.application.port.out.UpdateCustomerCredentialsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Customer jOOQ Adapter - Driven Adapter (Outbound)
 * jOOQ를 사용한 고객 인증 정보 조회/업데이트 구현
 * 컴파일 타임 타입 안전성 확보
 */
@Repository
@RequiredArgsConstructor
public class CustomerJooqAdapter implements LoadCustomerCredentialsPort, UpdateCustomerCredentialsPort {

    private final CustomerJooqRepository repository;

    // ==================== LoadCustomerCredentialsPort ====================

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findPasswordByEmail(String email) {
        return repository.findPasswordByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findPasswordByCustomerId(String customerId) {
        return repository.findPasswordById(customerId);
    }

    // ==================== UpdateCustomerCredentialsPort ====================

    @Override
    @Transactional
    public void updatePassword(String customerId, String encodedPassword) {
        repository.updatePassword(customerId, encodedPassword);
    }
}

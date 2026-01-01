package jjh.delivery.adapter.out.persistence.jpa;

import jjh.delivery.adapter.out.persistence.jpa.entity.CustomerAddressJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.CustomerJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.CustomerPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.CustomerJpaRepository;
import jjh.delivery.application.port.out.LoadCustomerCredentialsPort;
import jjh.delivery.application.port.out.LoadCustomerPort;
import jjh.delivery.application.port.out.SaveCustomerPort;
import jjh.delivery.domain.customer.Customer;
import jjh.delivery.domain.customer.CustomerAddress;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Customer JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 고객 저장/조회 구현
 */
@Component
public class CustomerJpaAdapter implements LoadCustomerPort, SaveCustomerPort, LoadCustomerCredentialsPort {

    private final CustomerJpaRepository repository;
    private final CustomerPersistenceMapper mapper;

    public CustomerJpaAdapter(CustomerJpaRepository repository, CustomerPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ==================== LoadCustomerPort ====================

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(String customerId) {
        return repository.findByIdWithAddresses(customerId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByEmail(String email) {
        return repository.findByEmailWithAddresses(email)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsById(String customerId) {
        return repository.existsById(customerId);
    }

    // ==================== SaveCustomerPort ====================

    @Override
    @Transactional
    public Customer save(Customer customer) {
        CustomerJpaEntity existing = repository.findByIdWithAddresses(customer.getId()).orElse(null);
        if (existing != null) {
            return updateExisting(existing, customer);
        }
        throw new IllegalStateException("Customer not found for update: " + customer.getId());
    }

    @Override
    @Transactional
    public Customer saveWithPassword(Customer customer, String encodedPassword) {
        CustomerJpaEntity entity = mapper.toEntity(customer, encodedPassword);
        CustomerJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void updatePassword(String customerId, String encodedPassword) {
        repository.updatePassword(customerId, encodedPassword);
    }

    @Override
    @Transactional
    public void delete(String customerId) {
        repository.deleteById(customerId);
    }

    private Customer updateExisting(CustomerJpaEntity existing, Customer customer) {
        // 기본 정보 업데이트
        existing.setEmail(customer.getEmail());
        existing.setName(customer.getName());
        existing.setPhoneNumber(customer.getPhoneNumber());
        existing.setStatus(customer.getStatus());
        existing.setUpdatedAt(customer.getUpdatedAt());
        existing.setLastLoginAt(customer.getLastLoginAt());

        // 주소 동기화
        syncAddresses(existing, customer);

        CustomerJpaEntity saved = repository.save(existing);
        return mapper.toDomain(saved);
    }

    private void syncAddresses(CustomerJpaEntity existing, Customer customer) {
        // 기존 주소 ID 집합
        Set<String> existingAddressIds = new HashSet<>();
        existing.getAddresses().forEach(addr -> existingAddressIds.add(addr.getId()));

        // 도메인 주소 ID 집합
        Set<String> domainAddressIds = new HashSet<>();
        customer.getAddresses().forEach(addr -> domainAddressIds.add(addr.id()));

        // 삭제할 주소 (기존에 있지만 도메인에 없는 것)
        existing.getAddresses().removeIf(addr -> !domainAddressIds.contains(addr.getId()));

        // 추가/업데이트할 주소
        for (CustomerAddress domainAddr : customer.getAddresses()) {
            CustomerAddressJpaEntity existingAddr = existing.getAddresses().stream()
                    .filter(addr -> addr.getId().equals(domainAddr.id()))
                    .findFirst()
                    .orElse(null);

            if (existingAddr != null) {
                // 기존 주소 업데이트
                existingAddr.setName(domainAddr.name());
                existingAddr.setRecipientName(domainAddr.recipientName());
                existingAddr.setPhoneNumber(domainAddr.phoneNumber());
                existingAddr.setPostalCode(domainAddr.postalCode());
                existingAddr.setAddress1(domainAddr.address1());
                existingAddr.setAddress2(domainAddr.address2());
                existingAddr.setDefault(domainAddr.isDefault());
            } else {
                // 새 주소 추가
                CustomerAddressJpaEntity newAddr = new CustomerAddressJpaEntity(
                        domainAddr.id(),
                        domainAddr.name(),
                        domainAddr.recipientName(),
                        domainAddr.phoneNumber(),
                        domainAddr.postalCode(),
                        domainAddr.address1(),
                        domainAddr.address2(),
                        domainAddr.isDefault(),
                        domainAddr.createdAt()
                );
                existing.addAddress(newAddr);
            }
        }
    }

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
}

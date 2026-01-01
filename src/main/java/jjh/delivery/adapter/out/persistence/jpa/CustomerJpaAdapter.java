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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        // Optional 체이닝으로 조회 및 업데이트 (함수형)
        return repository.findByIdWithAddresses(customer.getId())
                .map(existing -> updateExisting(existing, customer))
                .orElseThrow(() -> new IllegalStateException("Customer not found for update: " + customer.getId()));
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
        // Stream으로 ID 집합 수집 (함수형)
        Set<String> domainAddressIds = customer.getAddresses().stream()
                .map(CustomerAddress::id)
                .collect(Collectors.toSet());

        // 삭제할 주소 필터링 (기존에 있지만 도메인에 없는 것)
        existing.getAddresses().removeIf(addr -> !domainAddressIds.contains(addr.getId()));

        // ifPresentOrElse로 추가/업데이트 처리 (함수형)
        customer.getAddresses().forEach(domainAddr ->
                existing.getAddresses().stream()
                        .filter(addr -> addr.getId().equals(domainAddr.id()))
                        .findFirst()
                        .ifPresentOrElse(
                                existingAddr -> updateAddress(existingAddr, domainAddr),
                                () -> existing.addAddress(createAddressEntity(domainAddr))
                        )
        );
    }

    private void updateAddress(CustomerAddressJpaEntity entity, CustomerAddress domain) {
        entity.setName(domain.name());
        entity.setRecipientName(domain.recipientName());
        entity.setPhoneNumber(domain.phoneNumber());
        entity.setPostalCode(domain.postalCode());
        entity.setAddress1(domain.address1());
        entity.setAddress2(domain.address2());
        entity.setDefault(domain.isDefault());
    }

    private CustomerAddressJpaEntity createAddressEntity(CustomerAddress domain) {
        return new CustomerAddressJpaEntity(
                domain.id(),
                domain.name(),
                domain.recipientName(),
                domain.phoneNumber(),
                domain.postalCode(),
                domain.address1(),
                domain.address2(),
                domain.isDefault(),
                domain.createdAt()
        );
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

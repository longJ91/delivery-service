package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.out.persistence.jpa.entity.CustomerAddressJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.CustomerJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.CustomerPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.CustomerJpaRepository;
import jjh.delivery.application.port.out.LoadCustomerPort;
import jjh.delivery.application.port.out.SaveCustomerPort;
import jjh.delivery.domain.customer.Customer;
import jjh.delivery.domain.customer.CustomerAddress;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Customer JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 고객 저장/조회 구현
 * Note: 인증 정보 관련 기능은 CustomerJooqAdapter로 분리됨
 */
@Component
@RequiredArgsConstructor
public class CustomerJpaAdapter implements LoadCustomerPort, SaveCustomerPort {

    private final CustomerJpaRepository repository;
    private final CustomerPersistenceMapper mapper;

    // ==================== LoadCustomerPort ====================

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(UUID customerId) {
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
    public boolean existsById(UUID customerId) {
        return repository.existsById(customerId);
    }

    // ==================== SaveCustomerPort ====================

    @Override
    @Transactional
    public Customer save(Customer customer) {
        return repository.findById(customer.getId())
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
    public void delete(UUID customerId) {
        repository.deleteById(customerId);
    }

    private Customer updateExisting(CustomerJpaEntity existing, Customer customer) {
        existing.setEmail(customer.getEmail());
        existing.setName(customer.getName());
        existing.setPhoneNumber(customer.getPhoneNumber());
        existing.setProfileImageUrl(customer.getProfileImageUrl());
        existing.setStatus(customer.getStatus());
        existing.setUpdatedAt(customer.getUpdatedAt());
        existing.setLastLoginAt(customer.getLastLoginAt());

        syncAddresses(existing, customer);

        CustomerJpaEntity saved = repository.save(existing);
        return mapper.toDomain(saved);
    }

    private void syncAddresses(CustomerJpaEntity existing, Customer customer) {
        Set<UUID> domainAddressIds = customer.getAddresses().stream()
                .map(CustomerAddress::id)
                .collect(Collectors.toSet());

        existing.getAddresses().removeIf(addr -> !domainAddressIds.contains(addr.getId()));

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
}

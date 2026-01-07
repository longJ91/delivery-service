package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.CustomerAddressJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.CustomerJpaEntity;
import jjh.delivery.domain.customer.Customer;
import jjh.delivery.domain.customer.CustomerAddress;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Customer Persistence Mapper
 * Domain <-> JPA Entity 변환
 */
@Component
public class CustomerPersistenceMapper {

    public CustomerJpaEntity toEntity(Customer customer, String encodedPassword) {
        CustomerJpaEntity entity = new CustomerJpaEntity(
                customer.getId(),
                customer.getEmail(),
                encodedPassword,
                customer.getName(),
                customer.getPhoneNumber(),
                customer.getProfileImageUrl(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getLastLoginAt()
        );

        customer.getAddresses().forEach(address -> {
            CustomerAddressJpaEntity addressEntity = toAddressEntity(address);
            entity.addAddress(addressEntity);
        });

        return entity;
    }

    private CustomerAddressJpaEntity toAddressEntity(CustomerAddress address) {
        return new CustomerAddressJpaEntity(
                address.id(),
                address.name(),
                address.recipientName(),
                address.phoneNumber(),
                address.postalCode(),
                address.address1(),
                address.address2(),
                address.isDefault(),
                address.createdAt()
        );
    }

    public Customer toDomain(CustomerJpaEntity entity) {
        List<CustomerAddress> addresses = entity.getAddresses().stream()
                .map(this::toDomainAddress)
                .toList();

        return Customer.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .profileImageUrl(entity.getProfileImageUrl())
                .status(entity.getStatus())
                .addresses(addresses)
                .createdAt(entity.getCreatedAt())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }

    private CustomerAddress toDomainAddress(CustomerAddressJpaEntity entity) {
        return new CustomerAddress(
                entity.getId(),
                entity.getName(),
                entity.getRecipientName(),
                entity.getPhoneNumber(),
                entity.getPostalCode(),
                entity.getAddress1(),
                entity.getAddress2(),
                entity.isDefault(),
                entity.getCreatedAt()
        );
    }

    public List<Customer> toDomainList(List<CustomerJpaEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .toList();
    }
}

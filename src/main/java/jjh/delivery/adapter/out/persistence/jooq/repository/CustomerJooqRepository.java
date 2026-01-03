package jjh.delivery.adapter.out.persistence.jooq.repository;

import jjh.delivery.adapter.out.persistence.jooq.generated.tables.CustomerAddresses;
import jjh.delivery.adapter.out.persistence.jooq.generated.tables.Customers;
import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.CustomerAddressesRecord;
import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.CustomersRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.CustomerAddresses.CUSTOMER_ADDRESSES;
import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.Customers.CUSTOMERS;

/**
 * Customer jOOQ Repository - Type-safe queries
 * Replaces @Query methods in CustomerJpaRepository
 */
@Repository
@RequiredArgsConstructor
public class CustomerJooqRepository {

    private final DSLContext dsl;

    /**
     * Find customer by ID with addresses (replaces findByIdWithAddresses)
     * Compile-time type-safe version of:
     * SELECT c FROM CustomerJpaEntity c LEFT JOIN FETCH c.addresses WHERE c.id = :id
     */
    public Optional<CustomerWithAddresses> findByIdWithAddresses(UUID id) {
        Result<Record> result = dsl
                .select()
                .from(CUSTOMERS)
                .leftJoin(CUSTOMER_ADDRESSES)
                    .on(CUSTOMER_ADDRESSES.CUSTOMER_ID.eq(CUSTOMERS.ID))
                .where(CUSTOMERS.ID.eq(id))
                .fetch();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToCustomerWithAddresses(result));
    }

    /**
     * Find customer by email with addresses (replaces findByEmailWithAddresses)
     * Compile-time type-safe version of:
     * SELECT c FROM CustomerJpaEntity c LEFT JOIN FETCH c.addresses WHERE c.email = :email
     */
    public Optional<CustomerWithAddresses> findByEmailWithAddresses(String email) {
        Result<Record> result = dsl
                .select()
                .from(CUSTOMERS)
                .leftJoin(CUSTOMER_ADDRESSES)
                    .on(CUSTOMER_ADDRESSES.CUSTOMER_ID.eq(CUSTOMERS.ID))
                .where(CUSTOMERS.EMAIL.eq(email))
                .fetch();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToCustomerWithAddresses(result));
    }

    /**
     * Find password by email (replaces findPasswordByEmail)
     * Compile-time type-safe version of:
     * SELECT c.password FROM CustomerJpaEntity c WHERE c.email = :email
     */
    public Optional<String> findPasswordByEmail(String email) {
        return dsl
                .select(CUSTOMERS.PASSWORD)
                .from(CUSTOMERS)
                .where(CUSTOMERS.EMAIL.eq(email))
                .fetchOptional(CUSTOMERS.PASSWORD);
    }

    /**
     * Find password by ID (replaces findPasswordById)
     * Compile-time type-safe version of:
     * SELECT c.password FROM CustomerJpaEntity c WHERE c.id = :customerId
     */
    public Optional<String> findPasswordById(UUID customerId) {
        return dsl
                .select(CUSTOMERS.PASSWORD)
                .from(CUSTOMERS)
                .where(CUSTOMERS.ID.eq(customerId))
                .fetchOptional(CUSTOMERS.PASSWORD);
    }

    /**
     * Update password (replaces updatePassword)
     * Compile-time type-safe version of:
     * UPDATE CustomerJpaEntity c SET c.password = :password WHERE c.id = :customerId
     */
    public int updatePassword(UUID customerId, String password) {
        return dsl
                .update(CUSTOMERS)
                .set(CUSTOMERS.PASSWORD, password)
                .where(CUSTOMERS.ID.eq(customerId))
                .execute();
    }

    /**
     * Helper method to map result to CustomerWithAddresses
     */
    private CustomerWithAddresses mapToCustomerWithAddresses(Result<Record> result) {
        CustomersRecord customer = result.get(0).into(CUSTOMERS);
        List<CustomerAddressesRecord> addresses = result.stream()
                .filter(r -> r.get(CUSTOMER_ADDRESSES.ID) != null)
                .map(r -> r.into(CUSTOMER_ADDRESSES))
                .distinct()
                .toList();

        return new CustomerWithAddresses(customer, addresses);
    }

    /**
     * Result DTO for customer with addresses
     */
    public record CustomerWithAddresses(
            CustomersRecord customer,
            List<CustomerAddressesRecord> addresses
    ) {}
}

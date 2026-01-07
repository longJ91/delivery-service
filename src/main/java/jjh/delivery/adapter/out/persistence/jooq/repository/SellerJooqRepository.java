package jjh.delivery.adapter.out.persistence.jooq.repository;

import org.jooq.DSLContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.Sellers.SELLERS;

/**
 * Seller jOOQ Repository - Type-safe queries
 * Replaces @Query methods in SellerJpaRepository
 */
@Repository
@RequiredArgsConstructor
public class SellerJooqRepository {

    private final DSLContext dsl;

    /**
     * Find business name by ID (replaces findBusinessNameById)
     * Compile-time type-safe version of:
     * SELECT s.businessName FROM SellerJpaEntity s WHERE s.id = :id
     */
    public Optional<String> findBusinessNameById(UUID id) {
        return dsl
                .select(SELLERS.BUSINESS_NAME)
                .from(SELLERS)
                .where(SELLERS.ID.eq(id))
                .fetchOptional(SELLERS.BUSINESS_NAME);
    }

    /**
     * Check if seller exists by ID
     * Compile-time type-safe version of:
     * SELECT EXISTS(SELECT 1 FROM sellers WHERE id = :id)
     */
    public boolean existsById(UUID id) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(SELLERS)
                        .where(SELLERS.ID.eq(id))
        );
    }
}

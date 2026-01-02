package jjh.delivery.adapter.out.persistence.jooq.repository;

import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.CouponsRecord;
import org.jooq.DSLContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.Coupons.COUPONS;
import static org.jooq.impl.DSL.val;

/**
 * Coupon jOOQ Repository - Type-safe queries
 * Replaces @Query methods in CouponJpaRepository
 */
@Repository
@RequiredArgsConstructor
public class CouponJooqRepository {

    private final DSLContext dsl;

    /**
     * Find available coupons (replaces findAvailableCoupons)
     * Compile-time type-safe version of:
     * SELECT c FROM CouponJpaEntity c WHERE c.isActive = true
     * AND (c.validFrom IS NULL OR c.validFrom <= :now)
     * AND (c.validUntil IS NULL OR c.validUntil >= :now)
     * AND (c.totalQuantity = 0 OR c.usedQuantity < c.totalQuantity)
     */
    public List<CouponsRecord> findAvailableCoupons(LocalDateTime now) {
        return dsl
                .selectFrom(COUPONS)
                .where(COUPONS.IS_ACTIVE.eq(true))
                .and(COUPONS.VALID_FROM.isNull().or(COUPONS.VALID_FROM.le(now)))
                .and(COUPONS.VALID_UNTIL.isNull().or(COUPONS.VALID_UNTIL.ge(now)))
                .and(COUPONS.TOTAL_QUANTITY.eq(0).or(COUPONS.USED_QUANTITY.lt(COUPONS.TOTAL_QUANTITY)))
                .fetchInto(CouponsRecord.class);
    }
}

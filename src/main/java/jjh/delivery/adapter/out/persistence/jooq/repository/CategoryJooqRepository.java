package jjh.delivery.adapter.out.persistence.jooq.repository;

import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.CategoriesRecord;
import org.jooq.DSLContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.Categories.CATEGORIES;

/**
 * Category jOOQ Repository - Type-safe queries
 * Replaces @Query methods in CategoryJpaRepository
 */
@Repository
@RequiredArgsConstructor
public class CategoryJooqRepository {

    private final DSLContext dsl;

    /**
     * Find all active categories ordered by depth and display order
     * (replaces findAllActiveOrderByDepthAndDisplayOrder)
     * Compile-time type-safe version of:
     * SELECT c FROM CategoryJpaEntity c WHERE c.isActive = true ORDER BY c.depth, c.displayOrder
     */
    public List<CategoriesRecord> findAllActiveOrderByDepthAndDisplayOrder() {
        return dsl
                .selectFrom(CATEGORIES)
                .where(CATEGORIES.IS_ACTIVE.eq(true))
                .orderBy(CATEGORIES.DEPTH, CATEGORIES.DISPLAY_ORDER)
                .fetchInto(CategoriesRecord.class);
    }

    /**
     * Find root categories that are active (replaces findRootCategoriesActive)
     * Compile-time type-safe version of:
     * SELECT c FROM CategoryJpaEntity c
     * WHERE c.parentId IS NULL AND c.isActive = true ORDER BY c.displayOrder
     */
    public List<CategoriesRecord> findRootCategoriesActive() {
        return dsl
                .selectFrom(CATEGORIES)
                .where(CATEGORIES.PARENT_ID.isNull())
                .and(CATEGORIES.IS_ACTIVE.eq(true))
                .orderBy(CATEGORIES.DISPLAY_ORDER)
                .fetchInto(CategoriesRecord.class);
    }
}

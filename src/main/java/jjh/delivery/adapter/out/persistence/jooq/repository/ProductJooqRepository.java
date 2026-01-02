package jjh.delivery.adapter.out.persistence.jooq.repository;

import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.ProductVariantsRecord;
import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.ProductsRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.ProductCategories.PRODUCT_CATEGORIES;
import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.ProductVariants.PRODUCT_VARIANTS;
import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.Products.PRODUCTS;
import static org.jooq.impl.DSL.count;

/**
 * Product jOOQ Repository - Type-safe queries
 * Replaces @Query methods in ProductJpaRepository
 */
@Repository
@RequiredArgsConstructor
public class ProductJooqRepository {

    private final DSLContext dsl;

    /**
     * Find product by ID with variants (replaces findByIdWithVariants)
     * Compile-time type-safe version of:
     * SELECT DISTINCT p FROM ProductJpaEntity p LEFT JOIN FETCH p.variants WHERE p.id = :id
     */
    public Optional<ProductWithVariants> findByIdWithVariants(String id) {
        Result<Record> result = dsl
                .select()
                .from(PRODUCTS)
                .leftJoin(PRODUCT_VARIANTS)
                    .on(PRODUCT_VARIANTS.PRODUCT_ID.eq(PRODUCTS.ID))
                .where(PRODUCTS.ID.eq(id))
                .fetch();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToProductWithVariants(result));
    }

    /**
     * Count products by category ID and active status (replaces countByCategoryIdAndActive)
     * Compile-time type-safe version of:
     * SELECT COUNT(p) FROM ProductJpaEntity p
     * WHERE :categoryId MEMBER OF p.categoryIds AND p.status = 'ACTIVE'
     */
    public long countByCategoryIdAndActive(String categoryId) {
        return dsl
                .select(count())
                .from(PRODUCTS)
                .join(PRODUCT_CATEGORIES)
                    .on(PRODUCT_CATEGORIES.PRODUCT_ID.eq(PRODUCTS.ID))
                .where(PRODUCT_CATEGORIES.CATEGORY_ID.eq(categoryId))
                .and(PRODUCTS.STATUS.eq("ACTIVE"))
                .fetchOne(0, Long.class);
    }

    /**
     * Helper method to map result to ProductWithVariants
     */
    private ProductWithVariants mapToProductWithVariants(Result<Record> result) {
        ProductsRecord product = result.get(0).into(PRODUCTS);
        List<ProductVariantsRecord> variants = result.stream()
                .filter(r -> r.get(PRODUCT_VARIANTS.ID) != null)
                .map(r -> r.into(PRODUCT_VARIANTS))
                .distinct()
                .toList();

        return new ProductWithVariants(product, variants);
    }

    /**
     * Result DTO for product with variants
     */
    public record ProductWithVariants(
            ProductsRecord product,
            List<ProductVariantsRecord> variants
    ) {}
}

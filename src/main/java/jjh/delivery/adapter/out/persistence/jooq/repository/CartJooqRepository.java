package jjh.delivery.adapter.out.persistence.jooq.repository;

import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.CartItemsRecord;
import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.CartsRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.CartItems.CART_ITEMS;
import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.Carts.CARTS;

/**
 * Cart jOOQ Repository - Type-safe queries
 * Replaces @Query methods in CartJpaRepository
 */
@Repository
@RequiredArgsConstructor
public class CartJooqRepository {

    private final DSLContext dsl;

    /**
     * Find cart by customer ID with items (replaces findByCustomerIdWithItems)
     * Compile-time type-safe version of:
     * SELECT DISTINCT c FROM CartJpaEntity c LEFT JOIN FETCH c.items WHERE c.customerId = :customerId
     */
    public Optional<CartWithItems> findByCustomerIdWithItems(String customerId) {
        Result<Record> result = dsl
                .select()
                .from(CARTS)
                .leftJoin(CART_ITEMS)
                    .on(CART_ITEMS.CART_ID.eq(CARTS.ID))
                .where(CARTS.CUSTOMER_ID.eq(customerId))
                .fetch();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToCartWithItems(result));
    }

    /**
     * Helper method to map result to CartWithItems
     */
    private CartWithItems mapToCartWithItems(Result<Record> result) {
        CartsRecord cart = result.get(0).into(CARTS);
        List<CartItemsRecord> items = result.stream()
                .filter(r -> r.get(CART_ITEMS.ID) != null)
                .map(r -> r.into(CART_ITEMS))
                .distinct()
                .toList();

        return new CartWithItems(cart, items);
    }

    /**
     * Result DTO for cart with items
     */
    public record CartWithItems(
            CartsRecord cart,
            List<CartItemsRecord> items
    ) {}
}

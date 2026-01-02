package jjh.delivery.adapter.out.persistence.jooq.repository;

import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.OrderItemsRecord;
import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.OrdersRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.OrderItems.ORDER_ITEMS;
import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.Orders.ORDERS;

/**
 * Order jOOQ Repository - Type-safe queries
 * Replaces @Query methods in OrderJpaRepository
 */
@Repository
@RequiredArgsConstructor
public class OrderJooqRepository {

    private final DSLContext dsl;

    /**
     * Find order by ID with items (replaces findByIdWithItems)
     * Compile-time type-safe version of:
     * SELECT o FROM OrderJpaEntity o LEFT JOIN FETCH o.items WHERE o.id = :id
     */
    public Optional<OrderWithItems> findByIdWithItems(String id) {
        Result<Record> result = dsl
                .select()
                .from(ORDERS)
                .leftJoin(ORDER_ITEMS)
                    .on(ORDER_ITEMS.ORDER_ID.eq(ORDERS.ID))
                .where(ORDERS.ID.eq(id))
                .fetch();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToOrderWithItems(result).get(0));
    }

    /**
     * Find orders by customer ID with items (replaces findByCustomerIdWithItems)
     * Compile-time type-safe version of:
     * SELECT o FROM OrderJpaEntity o LEFT JOIN FETCH o.items
     * WHERE o.customerId = :customerId ORDER BY o.createdAt DESC
     */
    public List<OrderWithItems> findByCustomerIdWithItems(String customerId) {
        Result<Record> result = dsl
                .select()
                .from(ORDERS)
                .leftJoin(ORDER_ITEMS)
                    .on(ORDER_ITEMS.ORDER_ID.eq(ORDERS.ID))
                .where(ORDERS.CUSTOMER_ID.eq(customerId))
                .orderBy(ORDERS.CREATED_AT.desc())
                .fetch();

        return mapToOrderWithItems(result);
    }

    /**
     * Find orders by seller ID with items (replaces findBySellerIdWithItems)
     * Compile-time type-safe version of:
     * SELECT o FROM OrderJpaEntity o LEFT JOIN FETCH o.items
     * WHERE o.sellerId = :sellerId ORDER BY o.createdAt DESC
     */
    public List<OrderWithItems> findBySellerIdWithItems(String sellerId) {
        Result<Record> result = dsl
                .select()
                .from(ORDERS)
                .leftJoin(ORDER_ITEMS)
                    .on(ORDER_ITEMS.ORDER_ID.eq(ORDERS.ID))
                .where(ORDERS.SELLER_ID.eq(sellerId))
                .orderBy(ORDERS.CREATED_AT.desc())
                .fetch();

        return mapToOrderWithItems(result);
    }

    /**
     * Helper method to map result to list of OrderWithItems
     */
    private List<OrderWithItems> mapToOrderWithItems(Result<Record> result) {
        if (result.isEmpty()) {
            return List.of();
        }

        // Group by order ID
        Map<String, List<Record>> groupedByOrderId = result.stream()
                .collect(Collectors.groupingBy(r -> r.get(ORDERS.ID)));

        List<OrderWithItems> orders = new ArrayList<>();
        for (Map.Entry<String, List<Record>> entry : groupedByOrderId.entrySet()) {
            List<Record> records = entry.getValue();
            OrdersRecord order = records.get(0).into(ORDERS);
            List<OrderItemsRecord> items = records.stream()
                    .filter(r -> r.get(ORDER_ITEMS.ID) != null)
                    .map(r -> r.into(ORDER_ITEMS))
                    .toList();
            orders.add(new OrderWithItems(order, items));
        }

        // Sort by created_at DESC
        orders.sort((a, b) -> b.order().getCreatedAt().compareTo(a.order().getCreatedAt()));

        return orders;
    }

    /**
     * Result DTO for order with items
     */
    public record OrderWithItems(
            OrdersRecord order,
            List<OrderItemsRecord> items
    ) {}
}

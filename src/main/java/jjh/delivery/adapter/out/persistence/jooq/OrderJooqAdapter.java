package jjh.delivery.adapter.out.persistence.jooq;

import jjh.delivery.application.port.out.OrderQueryPort;
import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderItem;
import jjh.delivery.domain.order.OrderStatus;
import jjh.delivery.domain.order.ShippingAddress;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * Order jOOQ Adapter - Driven Adapter (Outbound)
 * jOOQ를 사용한 복잡한 쿼리 구현
 * v2 - Product Delivery
 */
@Component
public class OrderJooqAdapter implements OrderQueryPort {

    private final DSLContext dsl;

    public OrderJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Order> findOrdersWithComplexCriteria(ComplexQueryCriteria criteria) {
        List<Condition> conditions = new ArrayList<>();

        if (criteria.sellerIds() != null && !criteria.sellerIds().isEmpty()) {
            conditions.add(field("seller_id").in(criteria.sellerIds()));
        }

        if (criteria.statuses() != null && !criteria.statuses().isEmpty()) {
            List<String> statusStrings = criteria.statuses().stream()
                    .map(Enum::name)
                    .toList();
            conditions.add(field("status").in(statusStrings));
        }

        if (criteria.minAmount() != null) {
            conditions.add(field("total_amount").ge(criteria.minAmount()));
        }

        if (criteria.maxAmount() != null) {
            conditions.add(field("total_amount").le(criteria.maxAmount()));
        }

        if (criteria.fromDate() != null) {
            conditions.add(field("created_at").ge(criteria.fromDate()));
        }

        if (criteria.toDate() != null) {
            conditions.add(field("created_at").le(criteria.toDate()));
        }

        Condition combinedCondition = conditions.isEmpty()
                ? DSL.trueCondition()
                : conditions.stream().reduce(Condition::and).orElse(DSL.trueCondition());

        var orderField = criteria.ascending()
                ? field(criteria.sortBy() != null ? criteria.sortBy() : "created_at").asc()
                : field(criteria.sortBy() != null ? criteria.sortBy() : "created_at").desc();

        List<Record> records = dsl.select()
                .from(table("orders"))
                .where(combinedCondition)
                .orderBy(orderField)
                .offset(criteria.offset())
                .limit(criteria.limit())
                .fetch();

        List<String> orderIds = records.stream()
                .map(r -> r.get("id", String.class))
                .toList();

        Map<String, List<OrderItem>> itemsMap = fetchOrderItems(orderIds);

        return records.stream()
                .map(r -> mapToOrder(r, itemsMap.getOrDefault(r.get("id", String.class), List.of())))
                .toList();
    }

    @Override
    public List<OrderStatistics> getOrderStatisticsBySeller(
            String sellerId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return dsl.select(
                        field("seller_id"),
                        DSL.count().as("total_orders"),
                        DSL.count(DSL.case_()
                                .when(field("status").eq("DELIVERED"), 1)
                                .otherwise((Integer) null)).as("completed_orders"),
                        DSL.count(DSL.case_()
                                .when(field("status").eq("CANCELLED"), 1)
                                .otherwise((Integer) null)).as("cancelled_orders"),
                        DSL.sum(field("total_amount", BigDecimal.class)).as("total_revenue"),
                        DSL.avg(field("total_amount", BigDecimal.class)).as("avg_order_amount")
                )
                .from(table("orders"))
                .where(sellerId != null ? field("seller_id").eq(sellerId) : DSL.trueCondition())
                .and(from != null ? field("created_at").ge(from) : DSL.trueCondition())
                .and(to != null ? field("created_at").le(to) : DSL.trueCondition())
                .groupBy(field("seller_id"))
                .fetch()
                .map(r -> new OrderStatistics(
                        r.get("seller_id", String.class),
                        r.get("total_orders", Long.class),
                        r.get("completed_orders", Long.class),
                        r.get("cancelled_orders", Long.class),
                        r.get("total_revenue", BigDecimal.class),
                        r.get("avg_order_amount", BigDecimal.class)
                ));
    }

    @Override
    public List<Order> findOrdersForReport(ReportCriteria criteria) {
        List<Record> records = dsl.select()
                .from(table("orders"))
                .where(criteria.sellerId() != null
                        ? field("seller_id").eq(criteria.sellerId())
                        : DSL.trueCondition())
                .and(criteria.fromDate() != null
                        ? field("created_at").ge(criteria.fromDate())
                        : DSL.trueCondition())
                .and(criteria.toDate() != null
                        ? field("created_at").le(criteria.toDate())
                        : DSL.trueCondition())
                .orderBy(field("created_at").desc())
                .fetch();

        if (!criteria.includeItems()) {
            return records.stream()
                    .map(r -> mapToOrder(r, List.of()))
                    .toList();
        }

        List<String> orderIds = records.stream()
                .map(r -> r.get("id", String.class))
                .toList();

        Map<String, List<OrderItem>> itemsMap = fetchOrderItems(orderIds);

        return records.stream()
                .map(r -> mapToOrder(r, itemsMap.getOrDefault(r.get("id", String.class), List.of())))
                .toList();
    }

    private Map<String, List<OrderItem>> fetchOrderItems(List<String> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }

        return dsl.select()
                .from(table("order_items"))
                .where(field("order_id").in(orderIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        r -> r.get("order_id", String.class),
                        Collectors.mapping(
                                r -> OrderItem.of(
                                        r.get("product_id", String.class),
                                        r.get("product_name", String.class),
                                        r.get("quantity", Integer.class),
                                        r.get("unit_price", BigDecimal.class)
                                ),
                                Collectors.toList()
                        )
                ));
    }

    private Order mapToOrder(Record record, List<OrderItem> items) {
        return Order.builder()
                .id(record.get("id", String.class))
                .orderNumber(record.get("order_number", String.class))
                .customerId(record.get("customer_id", String.class))
                .sellerId(record.get("seller_id", String.class))
                .items(items)
                .status(OrderStatus.valueOf(record.get("status", String.class)))
                .shippingAddress(ShippingAddress.of(
                        record.get("shipping_recipient_name", String.class),
                        record.get("shipping_phone_number", String.class),
                        record.get("shipping_postal_code", String.class),
                        record.get("shipping_address1", String.class),
                        record.get("shipping_address2", String.class),
                        record.get("shipping_delivery_note", String.class)
                ))
                .createdAt(record.get("created_at", LocalDateTime.class))
                .build();
    }
}

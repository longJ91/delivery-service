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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * Order jOOQ Adapter - Driven Adapter (Outbound)
 * jOOQ를 사용한 복잡한 쿼리 구현
 * v2 - Product Delivery
 */
@Component
@RequiredArgsConstructor
public class OrderJooqAdapter implements OrderQueryPort {

    private final DSLContext dsl;

    @Override
    public List<Order> findOrdersWithComplexCriteria(ComplexQueryCriteria criteria) {
        // Stream + Optional로 동적 조건 구성 (함수형)
        Condition combinedCondition = Stream.of(
                        toCondition(criteria.sellerIds(), ids -> field("seller_id").in(ids)),
                        toCondition(criteria.statuses(),
                                statuses -> field("status").in(statuses.stream().map(Enum::name).toList())),
                        toCondition(criteria.minAmount(), amount -> field("total_amount").ge(amount)),
                        toCondition(criteria.maxAmount(), amount -> field("total_amount").le(amount)),
                        toCondition(criteria.fromDate(), date -> field("created_at").ge(date)),
                        toCondition(criteria.toDate(), date -> field("created_at").le(date))
                )
                .flatMap(Optional::stream)
                .reduce(Condition::and)
                .orElse(DSL.trueCondition());

        var sortField = Optional.ofNullable(criteria.sortBy()).orElse("created_at");
        var orderField = criteria.ascending()
                ? field(sortField).asc()
                : field(sortField).desc();

        List<Record> records = dsl.select()
                .from(table("orders"))
                .where(combinedCondition)
                .orderBy(orderField)
                .offset(criteria.offset())
                .limit(criteria.limit())
                .fetch();

        List<UUID> orderIds = records.stream()
                .map(r -> r.get("id", UUID.class))
                .toList();

        Map<UUID, List<OrderItem>> itemsMap = fetchOrderItems(orderIds);

        return records.stream()
                .map(r -> mapToOrder(r, itemsMap.getOrDefault(r.get("id", UUID.class), List.of())))
                .toList();
    }

    /**
     * 컬렉션 값이 null이 아니고 비어있지 않으면 조건 생성 (함수형 헬퍼)
     */
    private <T extends Iterable<?>> Optional<Condition> toCondition(T value, Function<T, Condition> mapper) {
        return Optional.ofNullable(value)
                .filter(v -> v.iterator().hasNext())
                .map(mapper);
    }

    /**
     * 단일 값이 null이 아니면 조건 생성 (함수형 헬퍼)
     */
    private <T> Optional<Condition> toCondition(T value, Function<T, Condition> mapper) {
        return Optional.ofNullable(value).map(mapper);
    }

    @Override
    public List<OrderStatistics> getOrderStatisticsBySeller(
            UUID sellerId,
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
                        r.get("seller_id", UUID.class),
                        r.get("total_orders", Long.class),
                        r.get("completed_orders", Long.class),
                        r.get("cancelled_orders", Long.class),
                        r.get("total_revenue", BigDecimal.class),
                        r.get("avg_order_amount", BigDecimal.class)
                ));
    }

    @Override
    public List<Order> findOrdersForReport(ReportCriteria criteria) {
        // Stream + Optional로 조건 구성 (함수형)
        Condition condition = Stream.of(
                        toCondition(criteria.sellerId(), id -> field("seller_id").eq(id)),
                        toCondition(criteria.fromDate(), date -> field("created_at").ge(date)),
                        toCondition(criteria.toDate(), date -> field("created_at").le(date))
                )
                .flatMap(Optional::stream)
                .reduce(Condition::and)
                .orElse(DSL.trueCondition());

        List<Record> records = dsl.select()
                .from(table("orders"))
                .where(condition)
                .orderBy(field("created_at").desc())
                .fetch();

        // 함수 합성: 아이템 포함 여부에 따라 다른 매핑 전략 (함수형)
        Function<Record, UUID> extractId = r -> r.get("id", UUID.class);
        Map<UUID, List<OrderItem>> itemsMap = criteria.includeItems()
                ? fetchOrderItems(records.stream().map(extractId).toList())
                : Map.of();

        return records.stream()
                .map(r -> mapToOrder(r, itemsMap.getOrDefault(extractId.apply(r), List.of())))
                .toList();
    }

    private Map<UUID, List<OrderItem>> fetchOrderItems(List<UUID> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }

        return dsl.select()
                .from(table("order_items"))
                .where(field("order_id").in(orderIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        r -> r.get("order_id", UUID.class),
                        Collectors.mapping(
                                r -> OrderItem.of(
                                        r.get("product_id", UUID.class),
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
                .id(record.get("id", UUID.class))
                .orderNumber(record.get("order_number", String.class))
                .customerId(record.get("customer_id", UUID.class))
                .sellerId(record.get("seller_id", UUID.class))
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

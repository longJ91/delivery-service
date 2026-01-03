package jjh.delivery.adapter.out.persistence.jooq.repository;

import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.WebhookDeliveriesRecord;
import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.WebhookSubscribedEventsRecord;
import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.WebhookSubscriptionsRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.WebhookDeliveries.WEBHOOK_DELIVERIES;
import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.WebhookSubscribedEvents.WEBHOOK_SUBSCRIBED_EVENTS;
import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.WebhookSubscriptions.WEBHOOK_SUBSCRIPTIONS;

/**
 * Webhook jOOQ Repository - Type-safe queries
 * Replaces @Query methods in WebhookSubscriptionJpaRepository and WebhookDeliveryJpaRepository
 */
@Repository
@RequiredArgsConstructor
public class WebhookJooqRepository {

    private final DSLContext dsl;

    /**
     * Find active subscriptions by event type (replaces findActiveByEventType)
     * Compile-time type-safe version of:
     * SELECT w FROM WebhookSubscriptionJpaEntity w
     * WHERE w.isActive = true AND :eventType MEMBER OF w.subscribedEvents
     */
    public List<WebhookSubscriptionWithEvents> findActiveByEventType(String eventType) {
        Result<Record> result = dsl
                .select()
                .from(WEBHOOK_SUBSCRIPTIONS)
                .join(WEBHOOK_SUBSCRIBED_EVENTS)
                    .on(WEBHOOK_SUBSCRIBED_EVENTS.SUBSCRIPTION_ID.eq(WEBHOOK_SUBSCRIPTIONS.ID))
                .where(WEBHOOK_SUBSCRIPTIONS.IS_ACTIVE.eq(true))
                .and(WEBHOOK_SUBSCRIBED_EVENTS.EVENT_TYPE.eq(eventType))
                .fetch();

        return mapToSubscriptionsWithEvents(result);
    }

    /**
     * Find pending retries (replaces findPendingRetries)
     * Compile-time type-safe version of:
     * SELECT d FROM WebhookDeliveryJpaEntity d
     * WHERE d.status = :status AND d.nextRetryAt <= :now
     */
    public List<WebhookDeliveriesRecord> findPendingRetries(String status, LocalDateTime now) {
        return dsl
                .selectFrom(WEBHOOK_DELIVERIES)
                .where(WEBHOOK_DELIVERIES.STATUS.eq(status))
                .and(WEBHOOK_DELIVERIES.NEXT_RETRY_AT.le(now))
                .fetchInto(WebhookDeliveriesRecord.class);
    }

    /**
     * Helper method to map result to list of WebhookSubscriptionWithEvents
     */
    private List<WebhookSubscriptionWithEvents> mapToSubscriptionsWithEvents(Result<Record> result) {
        if (result.isEmpty()) {
            return List.of();
        }

        // Group by subscription ID
        Map<UUID, List<Record>> groupedBySubscriptionId = result.stream()
                .collect(Collectors.groupingBy(r -> r.get(WEBHOOK_SUBSCRIPTIONS.ID)));

        return groupedBySubscriptionId.entrySet().stream()
                .map(entry -> {
                    List<Record> records = entry.getValue();
                    WebhookSubscriptionsRecord subscription = records.get(0).into(WEBHOOK_SUBSCRIPTIONS);
                    List<String> events = records.stream()
                            .map(r -> r.get(WEBHOOK_SUBSCRIBED_EVENTS.EVENT_TYPE))
                            .distinct()
                            .toList();
                    return new WebhookSubscriptionWithEvents(subscription, events);
                })
                .toList();
    }

    /**
     * Result DTO for webhook subscription with events
     */
    public record WebhookSubscriptionWithEvents(
            WebhookSubscriptionsRecord subscription,
            List<String> eventTypes
    ) {}
}

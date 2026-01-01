package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import jjh.delivery.domain.webhook.WebhookEventType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Webhook Subscription JPA Entity
 */
@Entity
@Table(name = "webhook_subscriptions", indexes = {
        @Index(name = "idx_webhook_subscriptions_seller_id", columnList = "seller_id"),
        @Index(name = "idx_webhook_subscriptions_is_active", columnList = "is_active")
})
public class WebhookSubscriptionJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "endpoint_url", nullable = false, length = 500)
    private String endpointUrl;

    @Column(nullable = false, length = 100)
    private String secret;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "webhook_subscribed_events",
            joinColumns = @JoinColumn(name = "subscription_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 50)
    private Set<WebhookEventType> subscribedEvents = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    @Column(name = "last_delivery_at")
    private LocalDateTime lastDeliveryAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    protected WebhookSubscriptionJpaEntity() {
    }

    public WebhookSubscriptionJpaEntity(
            String id,
            String sellerId,
            String name,
            String endpointUrl,
            String secret,
            Set<WebhookEventType> subscribedEvents,
            boolean isActive,
            int failureCount,
            LocalDateTime lastDeliveryAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.endpointUrl = endpointUrl;
        this.secret = secret;
        this.subscribedEvents = new HashSet<>(subscribedEvents);
        this.isActive = isActive;
        this.failureCount = failureCount;
        this.lastDeliveryAt = lastDeliveryAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public String getSecret() {
        return secret;
    }

    public Set<WebhookEventType> getSubscribedEvents() {
        return subscribedEvents;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public LocalDateTime getLastDeliveryAt() {
        return lastDeliveryAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}

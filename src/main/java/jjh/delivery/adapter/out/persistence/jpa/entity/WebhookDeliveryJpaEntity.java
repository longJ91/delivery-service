package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import jjh.delivery.domain.webhook.WebhookDeliveryStatus;
import jjh.delivery.domain.webhook.WebhookEventType;

import java.time.LocalDateTime;

/**
 * Webhook Delivery JPA Entity
 */
@Entity
@Table(name = "webhook_deliveries", indexes = {
        @Index(name = "idx_webhook_deliveries_subscription_id", columnList = "subscription_id"),
        @Index(name = "idx_webhook_deliveries_status", columnList = "status"),
        @Index(name = "idx_webhook_deliveries_created_at", columnList = "created_at")
})
public class WebhookDeliveryJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "subscription_id", nullable = false, length = 36)
    private String subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private WebhookEventType eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "endpoint_url", nullable = false, length = 500)
    private String endpointUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookDeliveryStatus status;

    @Column(name = "http_status_code")
    private int httpStatusCode;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    protected WebhookDeliveryJpaEntity() {
    }

    public WebhookDeliveryJpaEntity(
            String id,
            String subscriptionId,
            WebhookEventType eventType,
            String payload,
            String endpointUrl,
            WebhookDeliveryStatus status,
            int httpStatusCode,
            String responseBody,
            int attemptCount,
            LocalDateTime nextRetryAt,
            LocalDateTime createdAt,
            LocalDateTime deliveredAt
    ) {
        this.id = id;
        this.subscriptionId = subscriptionId;
        this.eventType = eventType;
        this.payload = payload;
        this.endpointUrl = endpointUrl;
        this.status = status;
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
        this.attemptCount = attemptCount;
        this.nextRetryAt = nextRetryAt;
        this.createdAt = createdAt;
        this.deliveredAt = deliveredAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public WebhookEventType getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public WebhookDeliveryStatus getStatus() {
        return status;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
}

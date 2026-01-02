package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import jjh.delivery.domain.webhook.WebhookDeliveryStatus;
import jjh.delivery.domain.webhook.WebhookEventType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Builder
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
}

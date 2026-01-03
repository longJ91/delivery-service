package jjh.delivery.adapter.in.web.webhook.dto;

import jjh.delivery.domain.webhook.WebhookDelivery;
import jjh.delivery.domain.webhook.WebhookDeliveryStatus;
import jjh.delivery.domain.webhook.WebhookEventType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 웹훅 전송 기록 응답
 */
public record WebhookDeliveryResponse(
        String id,
        String subscriptionId,
        WebhookEventType eventType,
        String endpointUrl,
        WebhookDeliveryStatus status,
        int httpStatusCode,
        int attemptCount,
        LocalDateTime nextRetryAt,
        LocalDateTime createdAt,
        LocalDateTime deliveredAt
) {
    public static WebhookDeliveryResponse from(WebhookDelivery delivery) {
        return new WebhookDeliveryResponse(
                delivery.getId().toString(),
                delivery.getSubscriptionId().toString(),
                delivery.getEventType(),
                delivery.getEndpointUrl(),
                delivery.getStatus(),
                delivery.getHttpStatusCode(),
                delivery.getAttemptCount(),
                delivery.getNextRetryAt(),
                delivery.getCreatedAt(),
                delivery.getDeliveredAt()
        );
    }
}

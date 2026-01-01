package jjh.delivery.adapter.in.web.webhook.dto;

import jjh.delivery.domain.webhook.WebhookEventType;
import jjh.delivery.domain.webhook.WebhookSubscription;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 웹훅 구독 응답
 */
public record WebhookSubscriptionResponse(
        String id,
        String sellerId,
        String name,
        String endpointUrl,
        String secret,
        Set<WebhookEventType> subscribedEvents,
        boolean isActive,
        boolean canDeliver,
        int failureCount,
        LocalDateTime lastDeliveryAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WebhookSubscriptionResponse from(WebhookSubscription subscription) {
        return new WebhookSubscriptionResponse(
                subscription.getId(),
                subscription.getSellerId(),
                subscription.getName(),
                subscription.getEndpointUrl(),
                subscription.getSecret(),
                subscription.getSubscribedEvents(),
                subscription.isActive(),
                subscription.canDeliver(),
                subscription.getFailureCount(),
                subscription.getLastDeliveryAt(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }

    /**
     * 시크릿을 마스킹한 응답 생성
     */
    public static WebhookSubscriptionResponse fromWithMaskedSecret(WebhookSubscription subscription) {
        String maskedSecret = subscription.getSecret().substring(0, 10) + "**********";

        return new WebhookSubscriptionResponse(
                subscription.getId(),
                subscription.getSellerId(),
                subscription.getName(),
                subscription.getEndpointUrl(),
                maskedSecret,
                subscription.getSubscribedEvents(),
                subscription.isActive(),
                subscription.canDeliver(),
                subscription.getFailureCount(),
                subscription.getLastDeliveryAt(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}

package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.WebhookDeliveryJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.WebhookSubscriptionJpaEntity;
import jjh.delivery.domain.webhook.WebhookDelivery;
import jjh.delivery.domain.webhook.WebhookSubscription;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Webhook Persistence Mapper
 * Domain <-> JPA Entity 변환
 */
@Component
public class WebhookPersistenceMapper {

    // ==================== Subscription ====================

    public WebhookSubscription toSubscriptionDomain(WebhookSubscriptionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return WebhookSubscription.builder()
                .id(entity.getId())
                .sellerId(entity.getSellerId())
                .name(entity.getName())
                .endpointUrl(entity.getEndpointUrl())
                .secret(entity.getSecret())
                .subscribedEvents(new HashSet<>(entity.getSubscribedEvents()))
                .isActive(entity.isActive())
                .failureCount(entity.getFailureCount())
                .lastDeliveryAt(entity.getLastDeliveryAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public WebhookSubscriptionJpaEntity toSubscriptionEntity(WebhookSubscription domain) {
        if (domain == null) {
            return null;
        }

        return new WebhookSubscriptionJpaEntity(
                domain.getId(),
                domain.getSellerId(),
                domain.getName(),
                domain.getEndpointUrl(),
                domain.getSecret(),
                new HashSet<>(domain.getSubscribedEvents()),
                domain.isActive(),
                domain.getFailureCount(),
                domain.getLastDeliveryAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    // ==================== Delivery ====================

    public WebhookDelivery toDeliveryDomain(WebhookDeliveryJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return WebhookDelivery.builder()
                .id(entity.getId())
                .subscriptionId(entity.getSubscriptionId())
                .eventType(entity.getEventType())
                .payload(entity.getPayload())
                .endpointUrl(entity.getEndpointUrl())
                .status(entity.getStatus())
                .httpStatusCode(entity.getHttpStatusCode())
                .responseBody(entity.getResponseBody())
                .attemptCount(entity.getAttemptCount())
                .nextRetryAt(entity.getNextRetryAt())
                .createdAt(entity.getCreatedAt())
                .deliveredAt(entity.getDeliveredAt())
                .build();
    }

    public WebhookDeliveryJpaEntity toDeliveryEntity(WebhookDelivery domain) {
        if (domain == null) {
            return null;
        }

        return new WebhookDeliveryJpaEntity(
                domain.getId(),
                domain.getSubscriptionId(),
                domain.getEventType(),
                domain.getPayload(),
                domain.getEndpointUrl(),
                domain.getStatus(),
                domain.getHttpStatusCode(),
                domain.getResponseBody(),
                domain.getAttemptCount(),
                domain.getNextRetryAt(),
                domain.getCreatedAt(),
                domain.getDeliveredAt()
        );
    }
}

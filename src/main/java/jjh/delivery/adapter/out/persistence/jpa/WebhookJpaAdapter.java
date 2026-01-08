package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.adapter.in.web.dto.CursorValue;
import jjh.delivery.adapter.out.persistence.jpa.entity.WebhookDeliveryJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.WebhookSubscriptionJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.WebhookPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.WebhookDeliveryJpaRepository;
import jjh.delivery.adapter.out.persistence.jpa.repository.WebhookSubscriptionJpaRepository;
import jjh.delivery.application.port.out.LoadWebhookPort;
import jjh.delivery.application.port.out.SaveWebhookPort;
import jjh.delivery.domain.webhook.WebhookDelivery;
import jjh.delivery.domain.webhook.WebhookDeliveryStatus;
import jjh.delivery.domain.webhook.WebhookEventType;
import jjh.delivery.domain.webhook.WebhookSubscription;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Webhook JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 웹훅 저장/조회 구현
 */
@Repository
@RequiredArgsConstructor
public class WebhookJpaAdapter implements LoadWebhookPort, SaveWebhookPort {

    private final WebhookSubscriptionJpaRepository subscriptionRepository;
    private final WebhookDeliveryJpaRepository deliveryRepository;
    private final WebhookPersistenceMapper mapper;

    // ==================== LoadWebhookPort - Subscription ====================

    @Override
    public Optional<WebhookSubscription> findSubscriptionById(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .map(mapper::toSubscriptionDomain);
    }

    @Override
    public List<WebhookSubscription> findSubscriptionsBySellerId(UUID sellerId) {
        return subscriptionRepository.findBySellerId(sellerId).stream()
                .map(mapper::toSubscriptionDomain)
                .toList();
    }

    @Override
    public List<WebhookSubscription> findActiveSubscriptionsByEventType(WebhookEventType eventType) {
        // JPA 사용 (@Query with entity mapping)
        return subscriptionRepository.findActiveByEventType(eventType).stream()
                .map(mapper::toSubscriptionDomain)
                .toList();
    }

    @Override
    public CursorPageResponse<WebhookSubscription> findAllSubscriptions(String cursor, int size) {
        CursorValue cursorValue = CursorValue.decode(cursor);

        List<WebhookSubscriptionJpaEntity> entities;
        if (cursorValue != null) {
            LocalDateTime cursorCreatedAt = LocalDateTime.ofInstant(cursorValue.createdAt(), ZoneId.systemDefault());
            entities = subscriptionRepository.findAllWithCursor(cursorCreatedAt, cursorValue.id(), size + 1);
        } else {
            entities = subscriptionRepository.findAllOrderByCreatedAtDesc(size + 1);
        }

        List<WebhookSubscription> subscriptions = entities.stream()
                .map(mapper::toSubscriptionDomain)
                .toList();

        return CursorPageResponse.ofWithUuidId(
                subscriptions,
                size,
                sub -> sub.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                WebhookSubscription::getId
        );
    }

    // ==================== LoadWebhookPort - Delivery ====================

    @Override
    public Optional<WebhookDelivery> findDeliveryById(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .map(mapper::toDeliveryDomain);
    }

    @Override
    public CursorPageResponse<WebhookDelivery> findDeliveriesBySubscriptionId(UUID subscriptionId, String cursor, int size) {
        CursorValue cursorValue = CursorValue.decode(cursor);

        List<WebhookDeliveryJpaEntity> entities;
        if (cursorValue != null) {
            LocalDateTime cursorCreatedAt = LocalDateTime.ofInstant(cursorValue.createdAt(), ZoneId.systemDefault());
            entities = deliveryRepository.findBySubscriptionIdWithCursor(subscriptionId, cursorCreatedAt, cursorValue.id(), size + 1);
        } else {
            entities = deliveryRepository.findBySubscriptionIdOrderByCreatedAtDesc(subscriptionId, size + 1);
        }

        List<WebhookDelivery> deliveries = entities.stream()
                .map(mapper::toDeliveryDomain)
                .toList();

        return CursorPageResponse.ofWithUuidId(
                deliveries,
                size,
                delivery -> delivery.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                WebhookDelivery::getId
        );
    }

    @Override
    public List<WebhookDelivery> findPendingRetries() {
        // JPA 사용 (@Query with entity mapping)
        return deliveryRepository.findPendingRetries(WebhookDeliveryStatus.RETRYING, LocalDateTime.now()).stream()
                .map(mapper::toDeliveryDomain)
                .toList();
    }

    // ==================== SaveWebhookPort ====================

    @Override
    public WebhookSubscription saveSubscription(WebhookSubscription subscription) {
        WebhookSubscriptionJpaEntity entity = mapper.toSubscriptionEntity(subscription);
        WebhookSubscriptionJpaEntity savedEntity = subscriptionRepository.save(entity);
        return mapper.toSubscriptionDomain(savedEntity);
    }

    @Override
    public void deleteSubscription(UUID subscriptionId) {
        subscriptionRepository.deleteById(subscriptionId);
    }

    @Override
    public WebhookDelivery saveDelivery(WebhookDelivery delivery) {
        WebhookDeliveryJpaEntity entity = mapper.toDeliveryEntity(delivery);
        WebhookDeliveryJpaEntity savedEntity = deliveryRepository.save(entity);
        return mapper.toDeliveryDomain(savedEntity);
    }
}

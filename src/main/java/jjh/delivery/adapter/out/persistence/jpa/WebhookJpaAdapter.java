package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
    public Page<WebhookSubscription> findAllSubscriptions(Pageable pageable) {
        return subscriptionRepository.findAll(pageable)
                .map(mapper::toSubscriptionDomain);
    }

    // ==================== LoadWebhookPort - Delivery ====================

    @Override
    public Optional<WebhookDelivery> findDeliveryById(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .map(mapper::toDeliveryDomain);
    }

    @Override
    public Page<WebhookDelivery> findDeliveriesBySubscriptionId(UUID subscriptionId, Pageable pageable) {
        return deliveryRepository.findBySubscriptionId(subscriptionId, pageable)
                .map(mapper::toDeliveryDomain);
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

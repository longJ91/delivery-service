package jjh.delivery.application.port.out;

import jjh.delivery.domain.webhook.WebhookDelivery;
import jjh.delivery.domain.webhook.WebhookEventType;
import jjh.delivery.domain.webhook.WebhookSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Load Webhook Port - Driven Port (Outbound)
 * 웹훅 조회 포트
 */
public interface LoadWebhookPort {

    // ==================== Subscription ====================

    Optional<WebhookSubscription> findSubscriptionById(UUID subscriptionId);

    List<WebhookSubscription> findSubscriptionsBySellerId(UUID sellerId);

    List<WebhookSubscription> findActiveSubscriptionsByEventType(WebhookEventType eventType);

    Page<WebhookSubscription> findAllSubscriptions(Pageable pageable);

    // ==================== Delivery ====================

    Optional<WebhookDelivery> findDeliveryById(UUID deliveryId);

    Page<WebhookDelivery> findDeliveriesBySubscriptionId(UUID subscriptionId, Pageable pageable);

    List<WebhookDelivery> findPendingRetries();
}

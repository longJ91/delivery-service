package jjh.delivery.application.port.out;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.webhook.WebhookDelivery;
import jjh.delivery.domain.webhook.WebhookEventType;
import jjh.delivery.domain.webhook.WebhookSubscription;

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

    CursorPageResponse<WebhookSubscription> findAllSubscriptions(String cursor, int size);

    // ==================== Delivery ====================

    Optional<WebhookDelivery> findDeliveryById(UUID deliveryId);

    CursorPageResponse<WebhookDelivery> findDeliveriesBySubscriptionId(UUID subscriptionId, String cursor, int size);

    List<WebhookDelivery> findPendingRetries();
}

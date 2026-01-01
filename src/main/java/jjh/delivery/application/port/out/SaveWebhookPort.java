package jjh.delivery.application.port.out;

import jjh.delivery.domain.webhook.WebhookDelivery;
import jjh.delivery.domain.webhook.WebhookSubscription;

/**
 * Save Webhook Port - Driven Port (Outbound)
 * 웹훅 저장 포트
 */
public interface SaveWebhookPort {

    WebhookSubscription saveSubscription(WebhookSubscription subscription);

    void deleteSubscription(String subscriptionId);

    WebhookDelivery saveDelivery(WebhookDelivery delivery);
}

package jjh.delivery.application.service;

import jjh.delivery.application.port.in.ManageWebhookUseCase;
import jjh.delivery.application.port.out.LoadWebhookPort;
import jjh.delivery.application.port.out.SaveWebhookPort;
import jjh.delivery.domain.webhook.WebhookDelivery;
import jjh.delivery.domain.webhook.WebhookEventType;
import jjh.delivery.domain.webhook.WebhookSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Webhook Service - Application Service
 * 웹훅 관리 서비스
 */
@Service
@Transactional
public class WebhookService implements ManageWebhookUseCase {

    private final LoadWebhookPort loadWebhookPort;
    private final SaveWebhookPort saveWebhookPort;

    public WebhookService(LoadWebhookPort loadWebhookPort, SaveWebhookPort saveWebhookPort) {
        this.loadWebhookPort = loadWebhookPort;
        this.saveWebhookPort = saveWebhookPort;
    }

    // ==================== Subscription 관리 ====================

    @Override
    public WebhookSubscription createSubscription(CreateSubscriptionCommand command) {
        WebhookSubscription subscription = WebhookSubscription.builder()
                .sellerId(command.sellerId())
                .name(command.name())
                .endpointUrl(command.endpointUrl())
                .subscribedEvents(new HashSet<>(command.subscribedEvents()))
                .isActive(true)
                .build();

        return saveWebhookPort.saveSubscription(subscription);
    }

    @Override
    public WebhookSubscription updateSubscription(UpdateSubscriptionCommand command) {
        WebhookSubscription subscription = getSubscriptionWithOwnerCheck(command.sellerId(), command.subscriptionId());

        // Optional + filter로 조건부 업데이트 (함수형)
        Optional.ofNullable(command.name())
                .filter(name -> !name.isBlank())
                .ifPresent(subscription::updateName);

        Optional.ofNullable(command.endpointUrl())
                .filter(url -> !url.isBlank())
                .ifPresent(subscription::updateEndpoint);

        // Stream으로 이벤트 구독 갱신 (함수형)
        Optional.ofNullable(command.subscribedEvents())
                .filter(events -> !events.isEmpty())
                .ifPresent(events -> {
                    Stream.of(WebhookEventType.values())
                            .forEach(subscription::unsubscribeEvent);
                    events.forEach(subscription::subscribeEvent);
                });

        return saveWebhookPort.saveSubscription(subscription);
    }

    @Override
    public void deleteSubscription(String sellerId, String subscriptionId) {
        getSubscriptionWithOwnerCheck(sellerId, subscriptionId);
        saveWebhookPort.deleteSubscription(subscriptionId);
    }

    @Override
    public WebhookSubscription activateSubscription(String sellerId, String subscriptionId) {
        WebhookSubscription subscription = getSubscriptionWithOwnerCheck(sellerId, subscriptionId);
        subscription.activate();
        return saveWebhookPort.saveSubscription(subscription);
    }

    @Override
    public WebhookSubscription deactivateSubscription(String sellerId, String subscriptionId) {
        WebhookSubscription subscription = getSubscriptionWithOwnerCheck(sellerId, subscriptionId);
        subscription.deactivate();
        return saveWebhookPort.saveSubscription(subscription);
    }

    @Override
    public WebhookSubscription regenerateSecret(String sellerId, String subscriptionId) {
        WebhookSubscription subscription = getSubscriptionWithOwnerCheck(sellerId, subscriptionId);
        subscription.regenerateSecret();
        return saveWebhookPort.saveSubscription(subscription);
    }

    // ==================== 조회 ====================

    @Override
    @Transactional(readOnly = true)
    public WebhookSubscription getSubscription(String subscriptionId) {
        return loadWebhookPort.findSubscriptionById(subscriptionId)
                .orElseThrow(() -> new NoSuchElementException("Webhook subscription not found: " + subscriptionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebhookSubscription> getSellerSubscriptions(String sellerId) {
        return loadWebhookPort.findSubscriptionsBySellerId(sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookSubscription> getAllSubscriptions(Pageable pageable) {
        return loadWebhookPort.findAllSubscriptions(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookDelivery> getDeliveryHistory(String subscriptionId, Pageable pageable) {
        return loadWebhookPort.findDeliveriesBySubscriptionId(subscriptionId, pageable);
    }

    @Override
    public WebhookDelivery testWebhook(String sellerId, String subscriptionId) {
        WebhookSubscription subscription = getSubscriptionWithOwnerCheck(sellerId, subscriptionId);

        // 테스트 페이로드 생성
        String testPayload = """
                {
                    "event": "test",
                    "message": "This is a test webhook delivery",
                    "timestamp": "%s"
                }
                """.formatted(java.time.LocalDateTime.now().toString());

        WebhookDelivery delivery = WebhookDelivery.builder()
                .subscriptionId(subscriptionId)
                .eventType(WebhookEventType.ORDER_CREATED) // 테스트용 이벤트
                .payload(testPayload)
                .endpointUrl(subscription.getEndpointUrl())
                .build();

        // 실제 전송 로직은 별도의 인프라 서비스에서 처리
        // 여기서는 전송 기록만 생성
        return saveWebhookPort.saveDelivery(delivery);
    }

    // ==================== Private Methods ====================

    private WebhookSubscription getSubscriptionWithOwnerCheck(String sellerId, String subscriptionId) {
        WebhookSubscription subscription = getSubscription(subscriptionId);

        if (!subscription.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("Subscription does not belong to the seller");
        }

        return subscription;
    }
}

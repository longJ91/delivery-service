package jjh.delivery.application.port.in;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.webhook.WebhookDelivery;
import jjh.delivery.domain.webhook.WebhookEventType;
import jjh.delivery.domain.webhook.WebhookSubscription;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Manage Webhook Use Case - Driving Port (Inbound)
 * 웹훅 관리 유스케이스
 */
public interface ManageWebhookUseCase {

    // ==================== Subscription 관리 ====================

    /**
     * 웹훅 구독 생성
     */
    WebhookSubscription createSubscription(CreateSubscriptionCommand command);

    /**
     * 웹훅 구독 수정
     */
    WebhookSubscription updateSubscription(UpdateSubscriptionCommand command);

    /**
     * 웹훅 구독 삭제
     */
    void deleteSubscription(UUID sellerId, UUID subscriptionId);

    /**
     * 웹훅 구독 활성화
     */
    WebhookSubscription activateSubscription(UUID sellerId, UUID subscriptionId);

    /**
     * 웹훅 구독 비활성화
     */
    WebhookSubscription deactivateSubscription(UUID sellerId, UUID subscriptionId);

    /**
     * 시크릿 재생성
     */
    WebhookSubscription regenerateSecret(UUID sellerId, UUID subscriptionId);

    // ==================== 조회 ====================

    /**
     * 구독 조회
     */
    WebhookSubscription getSubscription(UUID subscriptionId);

    /**
     * 판매자의 구독 목록 조회
     */
    List<WebhookSubscription> getSellerSubscriptions(UUID sellerId);

    /**
     * 전체 구독 목록 조회 (Admin, 커서 기반 페이지네이션)
     */
    CursorPageResponse<WebhookSubscription> getAllSubscriptions(String cursor, int size);

    /**
     * 전송 기록 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<WebhookDelivery> getDeliveryHistory(UUID subscriptionId, String cursor, int size);

    /**
     * 웹훅 테스트
     */
    WebhookDelivery testWebhook(UUID sellerId, UUID subscriptionId);

    // ==================== Commands ====================

    record CreateSubscriptionCommand(
            String sellerId,
            String name,
            String endpointUrl,
            Set<WebhookEventType> subscribedEvents
    ) {
        public CreateSubscriptionCommand {
            if (sellerId == null || sellerId.isBlank()) {
                throw new IllegalArgumentException("Seller ID is required");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (endpointUrl == null || endpointUrl.isBlank()) {
                throw new IllegalArgumentException("Endpoint URL is required");
            }
            if (subscribedEvents == null || subscribedEvents.isEmpty()) {
                throw new IllegalArgumentException("At least one event type is required");
            }
        }
    }

    record UpdateSubscriptionCommand(
            String sellerId,
            String subscriptionId,
            String name,
            String endpointUrl,
            Set<WebhookEventType> subscribedEvents
    ) {
        public UpdateSubscriptionCommand {
            if (sellerId == null || sellerId.isBlank()) {
                throw new IllegalArgumentException("Seller ID is required");
            }
            if (subscriptionId == null || subscriptionId.isBlank()) {
                throw new IllegalArgumentException("Subscription ID is required");
            }
        }
    }
}

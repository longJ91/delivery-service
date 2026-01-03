package jjh.delivery.domain.webhook;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Webhook Subscription Aggregate Root
 * 웹훅 구독 도메인 객체
 */
public class WebhookSubscription {

    private final UUID id;
    private final UUID sellerId;
    private String name;
    private String endpointUrl;
    private String secret;
    private final Set<WebhookEventType> subscribedEvents;
    private boolean isActive;
    private int failureCount;
    private LocalDateTime lastDeliveryAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private WebhookSubscription(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.sellerId = builder.sellerId;
        this.name = builder.name;
        this.endpointUrl = builder.endpointUrl;
        this.secret = builder.secret != null ? builder.secret : generateSecret();
        this.subscribedEvents = new HashSet<>(builder.subscribedEvents);
        this.isActive = builder.isActive;
        this.failureCount = builder.failureCount;
        this.lastDeliveryAt = builder.lastDeliveryAt;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 이벤트 구독 추가
     */
    public void subscribeEvent(WebhookEventType eventType) {
        subscribedEvents.add(eventType);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이벤트 구독 제거
     */
    public void unsubscribeEvent(WebhookEventType eventType) {
        subscribedEvents.remove(eventType);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 엔드포인트 URL 업데이트
     */
    public void updateEndpoint(String endpointUrl) {
        this.endpointUrl = endpointUrl;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이름 업데이트
     */
    public void updateName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 시크릿 재생성
     */
    public void regenerateSecret() {
        this.secret = generateSecret();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 활성화
     */
    public void activate() {
        this.isActive = true;
        this.failureCount = 0;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 전송 성공 기록
     */
    public void recordDeliverySuccess() {
        this.failureCount = 0;
        this.lastDeliveryAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 전송 실패 기록
     */
    public void recordDeliveryFailure() {
        this.failureCount++;
        this.updatedAt = LocalDateTime.now();

        // 연속 5회 실패 시 자동 비활성화
        if (this.failureCount >= 5) {
            this.isActive = false;
        }
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 특정 이벤트 구독 여부 확인
     */
    public boolean isSubscribedTo(WebhookEventType eventType) {
        return subscribedEvents.contains(eventType);
    }

    /**
     * 웹훅 전송 가능 여부 확인
     */
    public boolean canDeliver() {
        return isActive && failureCount < 5;
    }

    // =====================================================
    // Private Methods
    // =====================================================

    private static String generateSecret() {
        return "whsec_" + UUID.randomUUID().toString().replace("-", "");
    }

    // =====================================================
    // Getters
    // =====================================================

    public UUID getId() {
        return id;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public String getSecret() {
        return secret;
    }

    public Set<WebhookEventType> getSubscribedEvents() {
        return Collections.unmodifiableSet(subscribedEvents);
    }

    public boolean isActive() {
        return isActive;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public LocalDateTime getLastDeliveryAt() {
        return lastDeliveryAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private UUID id;
        private UUID sellerId;
        private String name;
        private String endpointUrl;
        private String secret;
        private Set<WebhookEventType> subscribedEvents = new HashSet<>();
        private boolean isActive = true;
        private int failureCount = 0;
        private LocalDateTime lastDeliveryAt;
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder sellerId(UUID sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder endpointUrl(String endpointUrl) {
            this.endpointUrl = endpointUrl;
            return this;
        }

        public Builder secret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder subscribedEvents(Set<WebhookEventType> subscribedEvents) {
            this.subscribedEvents = new HashSet<>(subscribedEvents);
            return this;
        }

        public Builder addEvent(WebhookEventType eventType) {
            this.subscribedEvents.add(eventType);
            return this;
        }

        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder failureCount(int failureCount) {
            this.failureCount = failureCount;
            return this;
        }

        public Builder lastDeliveryAt(LocalDateTime lastDeliveryAt) {
            this.lastDeliveryAt = lastDeliveryAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public WebhookSubscription build() {
            validateRequired();
            return new WebhookSubscription(this);
        }

        private void validateRequired() {
            if (sellerId == null) {
                throw new IllegalArgumentException("sellerId is required");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
            if (endpointUrl == null || endpointUrl.isBlank()) {
                throw new IllegalArgumentException("endpointUrl is required");
            }
        }
    }
}

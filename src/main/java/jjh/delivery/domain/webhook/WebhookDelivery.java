package jjh.delivery.domain.webhook;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Webhook Delivery Entity
 * 웹훅 전송 기록
 */
public class WebhookDelivery {

    private final String id;
    private final String subscriptionId;
    private final WebhookEventType eventType;
    private final String payload;
    private final String endpointUrl;
    private WebhookDeliveryStatus status;
    private int httpStatusCode;
    private String responseBody;
    private int attemptCount;
    private LocalDateTime nextRetryAt;
    private final LocalDateTime createdAt;
    private LocalDateTime deliveredAt;

    private WebhookDelivery(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.subscriptionId = builder.subscriptionId;
        this.eventType = builder.eventType;
        this.payload = builder.payload;
        this.endpointUrl = builder.endpointUrl;
        this.status = builder.status != null ? builder.status : WebhookDeliveryStatus.PENDING;
        this.httpStatusCode = builder.httpStatusCode;
        this.responseBody = builder.responseBody;
        this.attemptCount = builder.attemptCount;
        this.nextRetryAt = builder.nextRetryAt;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.deliveredAt = builder.deliveredAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 전송 성공 기록
     */
    public void markSuccess(int httpStatusCode, String responseBody) {
        this.status = WebhookDeliveryStatus.DELIVERED;
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
        this.attemptCount++;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * 전송 실패 기록
     */
    public void markFailed(int httpStatusCode, String responseBody) {
        this.attemptCount++;
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;

        if (attemptCount >= 3) {
            this.status = WebhookDeliveryStatus.FAILED;
        } else {
            this.status = WebhookDeliveryStatus.RETRYING;
            // 지수 백오프: 1분, 5분, 30분
            int delayMinutes = (int) Math.pow(5, attemptCount);
            this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
        }
    }

    /**
     * 재시도 가능 여부 확인
     */
    public boolean canRetry() {
        return status == WebhookDeliveryStatus.RETRYING &&
                attemptCount < 3 &&
                (nextRetryAt == null || LocalDateTime.now().isAfter(nextRetryAt));
    }

    // =====================================================
    // Getters
    // =====================================================

    public String getId() {
        return id;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public WebhookEventType getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public WebhookDeliveryStatus getStatus() {
        return status;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private String id;
        private String subscriptionId;
        private WebhookEventType eventType;
        private String payload;
        private String endpointUrl;
        private WebhookDeliveryStatus status;
        private int httpStatusCode;
        private String responseBody;
        private int attemptCount;
        private LocalDateTime nextRetryAt;
        private LocalDateTime createdAt;
        private LocalDateTime deliveredAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder subscriptionId(String subscriptionId) {
            this.subscriptionId = subscriptionId;
            return this;
        }

        public Builder eventType(WebhookEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder endpointUrl(String endpointUrl) {
            this.endpointUrl = endpointUrl;
            return this;
        }

        public Builder status(WebhookDeliveryStatus status) {
            this.status = status;
            return this;
        }

        public Builder httpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder responseBody(String responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Builder attemptCount(int attemptCount) {
            this.attemptCount = attemptCount;
            return this;
        }

        public Builder nextRetryAt(LocalDateTime nextRetryAt) {
            this.nextRetryAt = nextRetryAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder deliveredAt(LocalDateTime deliveredAt) {
            this.deliveredAt = deliveredAt;
            return this;
        }

        public WebhookDelivery build() {
            validateRequired();
            return new WebhookDelivery(this);
        }

        private void validateRequired() {
            if (subscriptionId == null || subscriptionId.isBlank()) {
                throw new IllegalArgumentException("subscriptionId is required");
            }
            if (eventType == null) {
                throw new IllegalArgumentException("eventType is required");
            }
            if (payload == null || payload.isBlank()) {
                throw new IllegalArgumentException("payload is required");
            }
            if (endpointUrl == null || endpointUrl.isBlank()) {
                throw new IllegalArgumentException("endpointUrl is required");
            }
        }
    }
}

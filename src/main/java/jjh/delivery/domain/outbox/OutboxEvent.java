package jjh.delivery.domain.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Outbox Event Entity
 * Transactional Outbox Pattern 구현을 위한 이벤트 저장 엔티티
 */
public class OutboxEvent {

    private static final int MAX_RETRY_COUNT = 3;

    private final UUID id;
    private final String aggregateType;     // Order, Product 등
    private final String aggregateId;       // 집합체 ID (Order ID 등)
    private final String eventType;         // OrderCreated, OrderStatusChanged 등
    private final String payload;           // JSON 직렬화된 이벤트 데이터
    private OutboxStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private int retryCount;
    private String errorMessage;

    private OutboxEvent(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.aggregateType = builder.aggregateType;
        this.aggregateId = builder.aggregateId;
        this.eventType = builder.eventType;
        this.payload = builder.payload;
        this.status = builder.status != null ? builder.status : OutboxStatus.PENDING;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.processedAt = builder.processedAt;
        this.retryCount = builder.retryCount;
        this.errorMessage = builder.errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 이벤트 발행 성공 처리
     */
    public void markAsSent() {
        this.status = OutboxStatus.SENT;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * 재시도 횟수 증가 및 에러 메시지 기록
     */
    public void incrementRetry(String errorMessage) {
        this.retryCount++;
        this.errorMessage = errorMessage;
    }

    /**
     * 이벤트 발행 실패 처리 (재시도 한도 초과)
     */
    public void markAsFailed() {
        this.status = OutboxStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 재시도 한도 초과 여부 확인
     */
    public boolean isRetryExhausted() {
        return retryCount >= MAX_RETRY_COUNT;
    }

    /**
     * 재시도 가능 여부 확인
     */
    public boolean canRetry() {
        return status == OutboxStatus.PENDING && retryCount < MAX_RETRY_COUNT;
    }

    // =====================================================
    // Getters
    // =====================================================

    public UUID getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private UUID id;
        private String aggregateType;
        private String aggregateId;
        private String eventType;
        private String payload;
        private OutboxStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;
        private int retryCount;
        private String errorMessage;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder status(OutboxStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public OutboxEvent build() {
            validateRequired();
            return new OutboxEvent(this);
        }

        private void validateRequired() {
            if (aggregateType == null || aggregateType.isBlank()) {
                throw new IllegalArgumentException("aggregateType is required");
            }
            if (aggregateId == null || aggregateId.isBlank()) {
                throw new IllegalArgumentException("aggregateId is required");
            }
            if (eventType == null || eventType.isBlank()) {
                throw new IllegalArgumentException("eventType is required");
            }
            if (payload == null || payload.isBlank()) {
                throw new IllegalArgumentException("payload is required");
            }
        }
    }
}

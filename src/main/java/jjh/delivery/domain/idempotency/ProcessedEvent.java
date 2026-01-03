package jjh.delivery.domain.idempotency;

import java.time.LocalDateTime;

/**
 * ProcessedEvent - Consumer Idempotency Domain
 * 이미 처리된 이벤트를 추적하여 중복 처리 방지
 */
public record ProcessedEvent(
        String eventId,
        String eventType,
        LocalDateTime processedAt
) {
    public static ProcessedEvent of(String eventId, String eventType) {
        return new ProcessedEvent(eventId, eventType, LocalDateTime.now());
    }
}

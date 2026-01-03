package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Processed Event JPA Entity
 * Consumer Idempotency 구현을 위한 엔티티
 */
@Entity
@Table(name = "processed_events", indexes = {
        @Index(name = "idx_processed_events_processed_at", columnList = "processed_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEventJpaEntity {

    @Id
    @Column(name = "event_id", length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Builder
    public ProcessedEventJpaEntity(String eventId, String eventType, LocalDateTime processedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.processedAt = processedAt;
    }
}

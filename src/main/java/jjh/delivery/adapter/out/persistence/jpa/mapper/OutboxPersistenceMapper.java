package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.OutboxEventJpaEntity;
import jjh.delivery.domain.outbox.OutboxEvent;
import org.springframework.stereotype.Component;

/**
 * Outbox Persistence Mapper
 * Domain <-> JPA Entity 변환
 */
@Component
public class OutboxPersistenceMapper {

    public OutboxEvent toDomain(OutboxEventJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return OutboxEvent.builder()
                .id(entity.getId())
                .aggregateType(entity.getAggregateType())
                .aggregateId(entity.getAggregateId())
                .eventType(entity.getEventType())
                .payload(entity.getPayload())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .processedAt(entity.getProcessedAt())
                .retryCount(entity.getRetryCount())
                .errorMessage(entity.getErrorMessage())
                .build();
    }

    public OutboxEventJpaEntity toEntity(OutboxEvent domain) {
        if (domain == null) {
            return null;
        }

        return OutboxEventJpaEntity.builder()
                .id(domain.getId())
                .aggregateType(domain.getAggregateType())
                .aggregateId(domain.getAggregateId())
                .eventType(domain.getEventType())
                .payload(domain.getPayload())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .processedAt(domain.getProcessedAt())
                .retryCount(domain.getRetryCount())
                .errorMessage(domain.getErrorMessage())
                .build();
    }
}

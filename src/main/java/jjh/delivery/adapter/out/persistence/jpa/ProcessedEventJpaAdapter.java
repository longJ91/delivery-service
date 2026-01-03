package jjh.delivery.adapter.out.persistence.jpa;

import jjh.delivery.adapter.out.persistence.jpa.entity.ProcessedEventJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.repository.ProcessedEventJpaRepository;
import jjh.delivery.application.port.out.ProcessedEventPort;
import jjh.delivery.domain.idempotency.ProcessedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Processed Event JPA Adapter - Driven Adapter (Outbound)
 * Consumer Idempotency를 위한 처리된 이벤트 저장/조회 구현
 */
@Repository
@RequiredArgsConstructor
public class ProcessedEventJpaAdapter implements ProcessedEventPort {

    private final ProcessedEventJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEventId(String eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    @Transactional
    public void save(ProcessedEvent processedEvent) {
        ProcessedEventJpaEntity entity = ProcessedEventJpaEntity.builder()
                .eventId(processedEvent.eventId())
                .eventType(processedEvent.eventType())
                .processedAt(processedEvent.processedAt())
                .build();
        repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteOldEvents(int retentionDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        repository.deleteByProcessedAtBefore(threshold);
    }
}

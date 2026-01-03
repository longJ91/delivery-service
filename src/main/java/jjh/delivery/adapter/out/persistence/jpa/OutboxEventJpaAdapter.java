package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.out.persistence.jpa.entity.OutboxEventJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.OutboxPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.OutboxEventJpaRepository;
import jjh.delivery.application.port.out.LoadOutboxEventPort;
import jjh.delivery.application.port.out.SaveOutboxEventPort;
import jjh.delivery.domain.outbox.OutboxEvent;
import jjh.delivery.domain.outbox.OutboxStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbox Event JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 Outbox 이벤트 저장/조회 구현
 */
@Repository
@RequiredArgsConstructor
public class OutboxEventJpaAdapter implements LoadOutboxEventPort, SaveOutboxEventPort {

    private final OutboxEventJpaRepository repository;
    private final OutboxPersistenceMapper mapper;

    // ==================== LoadOutboxEventPort ====================

    @Override
    public List<OutboxEvent> findPendingEvents(int limit) {
        return repository.findPendingEvents(limit).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<OutboxEvent> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    // ==================== SaveOutboxEventPort ====================

    @Override
    public OutboxEvent save(OutboxEvent event) {
        OutboxEventJpaEntity entity = mapper.toEntity(event);
        OutboxEventJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteOldSentEvents(int retentionDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        repository.deleteByStatusAndProcessedAtBefore(OutboxStatus.SENT, threshold);
    }
}

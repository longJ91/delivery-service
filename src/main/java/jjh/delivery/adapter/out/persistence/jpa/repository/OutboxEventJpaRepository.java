package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.OutboxEventJpaEntity;
import jjh.delivery.domain.outbox.OutboxStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Outbox Event JPA Repository
 */
@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    /**
     * 발행 대기 중인 이벤트 조회 (PENDING 상태, createdAt 오름차순)
     */
    @Query("SELECT e FROM OutboxEventJpaEntity e WHERE e.status = :status ORDER BY e.createdAt ASC")
    List<OutboxEventJpaEntity> findByStatus(@Param("status") OutboxStatus status, Pageable pageable);

    default List<OutboxEventJpaEntity> findPendingEvents(int limit) {
        return findByStatus(OutboxStatus.PENDING, PageRequest.of(0, limit));
    }

    /**
     * 오래된 SENT 상태 이벤트 삭제
     */
    @Modifying
    @Query("DELETE FROM OutboxEventJpaEntity e WHERE e.status = :status AND e.processedAt < :threshold")
    int deleteByStatusAndProcessedAtBefore(
            @Param("status") OutboxStatus status,
            @Param("threshold") LocalDateTime threshold);
}

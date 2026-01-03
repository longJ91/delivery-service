package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.ProcessedEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Processed Event JPA Repository
 * Consumer Idempotency 저장소
 */
@Repository
public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventJpaEntity, String> {

    /**
     * 이벤트 ID로 존재 여부 확인
     */
    boolean existsByEventId(String eventId);

    /**
     * 오래된 처리 기록 삭제
     */
    @Modifying
    @Query("DELETE FROM ProcessedEventJpaEntity p WHERE p.processedAt < :threshold")
    int deleteByProcessedAtBefore(@Param("threshold") LocalDateTime threshold);
}

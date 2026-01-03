package jjh.delivery.application.port.out;

import jjh.delivery.domain.outbox.OutboxEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Load Outbox Event Port - Driven Port (Outbound)
 * Outbox 이벤트 조회를 위한 포트
 */
public interface LoadOutboxEventPort {

    /**
     * 발행 대기 중인 이벤트 조회
     *
     * @param limit 최대 조회 개수
     * @return PENDING 상태인 이벤트 목록 (createdAt 오름차순)
     */
    List<OutboxEvent> findPendingEvents(int limit);

    /**
     * ID로 이벤트 조회
     */
    Optional<OutboxEvent> findById(UUID id);
}

package jjh.delivery.application.port.out;

import jjh.delivery.domain.outbox.OutboxEvent;

/**
 * Save Outbox Event Port - Driven Port (Outbound)
 * Outbox 이벤트 저장을 위한 포트
 */
public interface SaveOutboxEventPort {

    /**
     * Outbox 이벤트 저장
     */
    OutboxEvent save(OutboxEvent event);

    /**
     * Outbox 이벤트 삭제
     */
    void deleteOldSentEvents(int retentionDays);
}

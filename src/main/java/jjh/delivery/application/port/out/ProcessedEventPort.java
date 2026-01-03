package jjh.delivery.application.port.out;

import jjh.delivery.domain.idempotency.ProcessedEvent;

/**
 * ProcessedEventPort - Consumer Idempotency Port
 * 이벤트 중복 처리 방지를 위한 아웃바운드 포트
 */
public interface ProcessedEventPort {

    /**
     * 이벤트가 이미 처리되었는지 확인
     */
    boolean existsByEventId(String eventId);

    /**
     * 처리된 이벤트 저장
     */
    void save(ProcessedEvent processedEvent);

    /**
     * 오래된 처리 기록 삭제 (정리 작업용)
     */
    void deleteOldEvents(int retentionDays);
}

package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jjh.delivery.application.port.out.SaveOutboxEventPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox Event Cleaner
 * 오래된 SENT 상태 이벤트를 정리하여 테이블 크기 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventCleaner {

    private final SaveOutboxEventPort saveOutboxEventPort;

    @Value("${outbox.cleanup.retention-days:7}")
    private int retentionDays;

    /**
     * 오래된 SENT 이벤트 삭제
     * 매일 새벽 3시 실행
     */
    @Scheduled(cron = "${outbox.cleanup.cron:0 0 3 * * ?}")
    @Transactional
    public void cleanupOldEvents() {
        log.info("Starting outbox cleanup. Retention days: {}", retentionDays);

        try {
            saveOutboxEventPort.deleteOldSentEvents(retentionDays);
            log.info("Outbox cleanup completed successfully");
        } catch (Exception e) {
            log.error("Outbox cleanup failed", e);
        }
    }
}

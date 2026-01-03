package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jjh.delivery.application.port.out.LoadOutboxEventPort;
import jjh.delivery.application.port.out.SaveOutboxEventPort;
import jjh.delivery.domain.outbox.OutboxEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Outbox Event Publisher
 * 스케줄러를 통해 Outbox 테이블의 PENDING 이벤트를 Kafka로 발행
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final LoadOutboxEventPort loadOutboxEventPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${outbox.publisher.batch-size:100}")
    private int batchSize;

    @Value("${outbox.publisher.timeout-seconds:10}")
    private int timeoutSeconds;

    /**
     * PENDING 상태의 이벤트를 Kafka로 발행
     * 1초마다 실행 (fixedDelay)
     */
    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay:1000}")
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = loadOutboxEventPort.findPendingEvents(batchSize);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            processEvent(event);
        }
    }

    /**
     * 개별 이벤트 처리 (각 이벤트별 독립 트랜잭션)
     */
    @Transactional
    protected void processEvent(OutboxEvent event) {
        try {
            sendToKafka(event);
            event.markAsSent();
            log.info("Successfully published outbox event: id={}, type={}, aggregateId={}",
                    event.getId(), event.getEventType(), event.getAggregateId());
        } catch (Exception e) {
            log.error("Failed to publish outbox event: id={}, type={}, error={}",
                    event.getId(), event.getEventType(), e.getMessage());
            event.incrementRetry(e.getMessage());

            if (event.isRetryExhausted()) {
                event.markAsFailed();
                log.error("Outbox event marked as FAILED after max retries: id={}", event.getId());
            }
        }
        saveOutboxEventPort.save(event);
    }

    /**
     * Kafka로 이벤트 전송 (동기)
     */
    private void sendToKafka(OutboxEvent event) throws ExecutionException, InterruptedException, TimeoutException {
        String topic = resolveTopic(event);
        String key = event.getAggregateId();
        String payload = event.getPayload();

        // 동기 전송: 응답 대기하여 전송 성공 확인
        kafkaTemplate.send(topic, key, payload)
                .get(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * 이벤트 타입에 따른 토픽 결정
     */
    private String resolveTopic(OutboxEvent event) {
        return switch (event.getEventType()) {
            case "OrderCreated" -> "order.created";
            case "OrderStatusChanged" -> "order.status-changed";
            default -> throw new IllegalArgumentException(
                    "Unknown event type: " + event.getEventType());
        };
    }
}

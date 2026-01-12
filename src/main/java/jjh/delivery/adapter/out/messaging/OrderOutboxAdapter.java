package jjh.delivery.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jjh.delivery.application.port.out.OrderEventPort;
import jjh.delivery.application.port.out.SaveOutboxEventPort;
import jjh.delivery.domain.order.event.OrderCreatedEvent;
import jjh.delivery.domain.order.event.OrderEvent;
import jjh.delivery.domain.order.event.OrderStatusChangedEvent;
import jjh.delivery.domain.outbox.OutboxEvent;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Order Outbox Adapter - Driven Adapter (Outbound)
 * Transactional Outbox Pattern을 사용한 이벤트 발행 구현
 *
 * 이벤트를 outbox 테이블에 저장하고, OutboxEventPublisher 스케줄러가 Kafka로 발행
 *
 * Jackson 3 변경사항:
 * - ObjectMapper → JsonMapper
 * - JsonProcessingException (checked) → JacksonException (unchecked)
 * - try-catch 블록 불필요 (unchecked exception 자동 전파)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderOutboxAdapter implements OrderEventPort {

    private static final String AGGREGATE_TYPE = "Order";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final JsonMapper jsonMapper;

    @Override
    public void publish(OrderEvent event) {
        OutboxEvent outboxEvent = toOutboxEvent(event);
        saveOutboxEventPort.save(outboxEvent);
        log.info("Saved outbox event: type={}, aggregateId={}",
                outboxEvent.getEventType(), outboxEvent.getAggregateId());
    }

    @Override
    public void publishAsync(OrderEvent event) {
        // Outbox 패턴에서는 동기/비동기 구분이 없음
        // 이벤트는 트랜잭션 내에서 DB에 저장되고,
        // 별도 스케줄러가 Kafka로 발행
        publish(event);
    }

    /**
     * OrderEvent를 OutboxEvent로 변환
     */
    private OutboxEvent toOutboxEvent(OrderEvent event) {
        String eventType = resolveEventType(event);
        String payload = serializeToJson(event);

        return OutboxEvent.builder()
                .aggregateType(AGGREGATE_TYPE)
                .aggregateId(event.orderId().toString())
                .eventType(eventType)
                .payload(payload)
                .build();
    }

    /**
     * 이벤트 타입 결정
     */
    private String resolveEventType(OrderEvent event) {
        return switch (event) {
            case OrderCreatedEvent e -> "OrderCreated";
            case OrderStatusChangedEvent e -> "OrderStatusChanged";
        };
    }

    /**
     * 이벤트를 JSON 문자열로 직렬화
     *
     * Jackson 3에서는 JacksonException이 unchecked exception이므로
     * try-catch 블록 없이 예외가 자동 전파됨
     */
    private String serializeToJson(OrderEvent event) {
        return jsonMapper.writeValueAsString(event);
    }
}

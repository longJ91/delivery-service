package jjh.delivery.adapter.out.messaging;

import jjh.delivery.application.port.out.OrderEventPort;
import jjh.delivery.domain.order.event.OrderCreatedEvent;
import jjh.delivery.domain.order.event.OrderEvent;
import jjh.delivery.domain.order.event.OrderStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Order Kafka Adapter - Driven Adapter (Outbound)
 * Kafka를 사용한 이벤트 발행 구현
 */
@Component
@RequiredArgsConstructor
public class OrderKafkaAdapter implements OrderEventPort {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaAdapter.class);

    private static final String TOPIC_ORDER_CREATED = "order.created";
    private static final String TOPIC_ORDER_STATUS_CHANGED = "order.status-changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(OrderEvent event) {
        String topic = resolveTopic(event);
        String key = event.orderId().toString();

        try {
            kafkaTemplate.send(topic, key, event).get();
            log.info("Published event to topic {}: {}", topic, event);
        } catch (Exception e) {
            log.error("Failed to publish event to topic {}: {}", topic, event, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Override
    public void publishAsync(OrderEvent event) {
        String topic = resolveTopic(event);
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic {}: {}", topic, event, ex);
            } else {
                log.info("Published event to topic {} at offset {}: {}",
                        topic,
                        result.getRecordMetadata().offset(),
                        event);
            }
        });
    }

    private String resolveTopic(OrderEvent event) {
        return switch (event) {
            case OrderCreatedEvent e -> TOPIC_ORDER_CREATED;
            case OrderStatusChangedEvent e -> TOPIC_ORDER_STATUS_CHANGED;
        };
    }
}

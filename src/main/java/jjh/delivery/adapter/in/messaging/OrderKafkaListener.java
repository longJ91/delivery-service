package jjh.delivery.adapter.in.messaging;

import jjh.delivery.application.port.in.UpdateOrderStatusUseCase;
import jjh.delivery.application.port.out.ProcessedEventPort;
import jjh.delivery.domain.idempotency.ProcessedEvent;
import jjh.delivery.domain.order.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Order Kafka Listener - Driving Adapter (Inbound)
 * 외부 시스템으로부터의 이벤트 수신 (v2 - Product Delivery)
 *
 * Consumer Idempotency: eventId 헤더 기반 중복 처리 방지
 */
@Component
@RequiredArgsConstructor
public class OrderKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaListener.class);
    private static final String EVENT_ID_HEADER = "eventId";
    private static final String EVENT_TYPE_HEADER = "eventType";

    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final ProcessedEventPort processedEventPort;

    /**
     * 배송 출발 이벤트 수신
     * 배송 서비스에서 주문을 출발 처리했을 때 호출
     */
    @KafkaListener(
            topics = "shipment.out-for-delivery",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleOutForDelivery(
            @Payload ShipmentOutForDeliveryEvent event,
            @Headers MessageHeaders headers,
            Acknowledgment acknowledgment
    ) {
        String eventId = extractHeader(headers, EVENT_ID_HEADER);
        String eventType = extractHeader(headers, EVENT_TYPE_HEADER);

        log.info("Received out for delivery event: orderId={}, eventId={}", event.orderId(), eventId);

        // 멱등성 체크: 이미 처리된 이벤트면 스킵
        if (eventId != null && isAlreadyProcessed(eventId)) {
            log.info("Skipping duplicate event: eventId={}", eventId);
            acknowledgment.acknowledge();
            return;
        }

        try {
            updateOrderStatusUseCase.updateStatus(UUID.fromString(event.orderId()), OrderStatus.OUT_FOR_DELIVERY);

            // 처리 완료 기록
            if (eventId != null) {
                markAsProcessed(eventId, eventType);
            }

            acknowledgment.acknowledge();
            log.info("Successfully processed out for delivery event: orderId={}, eventId={}", event.orderId(), eventId);
        } catch (Exception e) {
            log.error("Failed to process out for delivery event: orderId={}, eventId={}", event.orderId(), eventId, e);
            throw e;
        }
    }

    /**
     * 배송 완료 이벤트 수신
     */
    @KafkaListener(
            topics = "shipment.delivered",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleDeliveryCompleted(
            @Payload ShipmentDeliveredEvent event,
            @Headers MessageHeaders headers,
            Acknowledgment acknowledgment
    ) {
        String eventId = extractHeader(headers, EVENT_ID_HEADER);
        String eventType = extractHeader(headers, EVENT_TYPE_HEADER);

        log.info("Received delivery completed event: orderId={}, eventId={}", event.orderId(), eventId);

        // 멱등성 체크
        if (eventId != null && isAlreadyProcessed(eventId)) {
            log.info("Skipping duplicate event: eventId={}", eventId);
            acknowledgment.acknowledge();
            return;
        }

        try {
            updateOrderStatusUseCase.updateStatus(UUID.fromString(event.orderId()), OrderStatus.DELIVERED);

            if (eventId != null) {
                markAsProcessed(eventId, eventType);
            }

            acknowledgment.acknowledge();
            log.info("Successfully processed delivery completed event: orderId={}, eventId={}", event.orderId(), eventId);
        } catch (Exception e) {
            log.error("Failed to process delivery completed event: orderId={}, eventId={}", event.orderId(), eventId, e);
            throw e;
        }
    }

    /**
     * 배송 중 이벤트 수신 (허브 이동)
     */
    @KafkaListener(
            topics = "shipment.in-transit",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInTransit(
            @Payload ShipmentInTransitEvent event,
            @Headers MessageHeaders headers,
            Acknowledgment acknowledgment
    ) {
        String eventId = extractHeader(headers, EVENT_ID_HEADER);
        String eventType = extractHeader(headers, EVENT_TYPE_HEADER);

        log.info("Received in transit event: orderId={}, eventId={}", event.orderId(), eventId);

        // 멱등성 체크
        if (eventId != null && isAlreadyProcessed(eventId)) {
            log.info("Skipping duplicate event: eventId={}", eventId);
            acknowledgment.acknowledge();
            return;
        }

        try {
            updateOrderStatusUseCase.updateStatus(UUID.fromString(event.orderId()), OrderStatus.IN_TRANSIT);

            if (eventId != null) {
                markAsProcessed(eventId, eventType);
            }

            acknowledgment.acknowledge();
            log.info("Successfully processed in transit event: orderId={}, eventId={}", event.orderId(), eventId);
        } catch (Exception e) {
            log.error("Failed to process in transit event: orderId={}, eventId={}", event.orderId(), eventId, e);
            throw e;
        }
    }

    // ==================== Helper Methods ====================

    /**
     * 이벤트가 이미 처리되었는지 확인
     */
    private boolean isAlreadyProcessed(String eventId) {
        return processedEventPort.existsByEventId(eventId);
    }

    /**
     * 이벤트를 처리 완료로 기록
     */
    private void markAsProcessed(String eventId, String eventType) {
        ProcessedEvent processedEvent = ProcessedEvent.of(eventId, eventType != null ? eventType : "UNKNOWN");
        processedEventPort.save(processedEvent);
    }

    /**
     * MessageHeaders에서 특정 헤더 값 추출
     */
    private String extractHeader(MessageHeaders headers, String headerName) {
        Object headerValue = headers.get(headerName);
        if (headerValue instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        } else if (headerValue instanceof String str) {
            return str;
        }
        return null;
    }

    // ==================== Event DTOs ====================

    public record ShipmentOutForDeliveryEvent(
            String orderId,
            String shipmentId,
            String carrierId
    ) {}

    public record ShipmentDeliveredEvent(
            String orderId,
            String shipmentId,
            String carrierId,
            java.time.LocalDateTime deliveredAt
    ) {}

    public record ShipmentInTransitEvent(
            String orderId,
            String shipmentId,
            String carrierId,
            String currentLocation
    ) {}
}

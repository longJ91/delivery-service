package jjh.delivery.adapter.in.messaging;

import jjh.delivery.application.port.in.UpdateOrderStatusUseCase;
import jjh.delivery.domain.order.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Order Kafka Listener - Driving Adapter (Inbound)
 * 외부 시스템으로부터의 이벤트 수신 (v2 - Product Delivery)
 */
@Component
@RequiredArgsConstructor
public class OrderKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaListener.class);

    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    /**
     * 배송 출발 이벤트 수신
     * 배송 서비스에서 주문을 출발 처리했을 때 호출
     */
    @KafkaListener(
            topics = "shipment.out-for-delivery",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOutForDelivery(
            @Payload ShipmentOutForDeliveryEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("Received out for delivery event: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset);

        try {
            updateOrderStatusUseCase.updateStatus(event.orderId(), OrderStatus.OUT_FOR_DELIVERY);
            acknowledgment.acknowledge();
            log.info("Successfully processed out for delivery event: orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to process out for delivery event: orderId={}", event.orderId(), e);
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
    public void handleDeliveryCompleted(
            @Payload ShipmentDeliveredEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment
    ) {
        log.info("Received delivery completed event: orderId={}", event.orderId());

        try {
            updateOrderStatusUseCase.updateStatus(event.orderId(), OrderStatus.DELIVERED);
            acknowledgment.acknowledge();
            log.info("Successfully processed delivery completed event: orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to process delivery completed event: orderId={}", event.orderId(), e);
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
    public void handleInTransit(
            @Payload ShipmentInTransitEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment
    ) {
        log.info("Received in transit event: orderId={}", event.orderId());

        try {
            updateOrderStatusUseCase.updateStatus(event.orderId(), OrderStatus.IN_TRANSIT);
            acknowledgment.acknowledge();
            log.info("Successfully processed in transit event: orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to process in transit event: orderId={}", event.orderId(), e);
            throw e;
        }
    }

    // Event DTOs
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

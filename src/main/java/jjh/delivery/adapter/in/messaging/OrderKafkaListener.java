package jjh.delivery.adapter.in.messaging;

import jjh.delivery.application.port.in.UpdateOrderStatusUseCase;
import jjh.delivery.domain.order.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Order Kafka Listener - Driving Adapter (Inbound)
 * 외부 시스템으로부터의 이벤트 수신
 */
@Component
public class OrderKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaListener.class);

    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    public OrderKafkaListener(UpdateOrderStatusUseCase updateOrderStatusUseCase) {
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
    }

    /**
     * 배달 픽업 이벤트 수신
     * 배달 서비스에서 주문을 픽업했을 때 호출
     */
    @KafkaListener(
            topics = "delivery.picked-up",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryPickedUp(
            @Payload DeliveryPickedUpEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("Received delivery picked up event: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset);

        try {
            updateOrderStatusUseCase.updateStatus(event.orderId(), OrderStatus.PICKED_UP);
            acknowledgment.acknowledge();
            log.info("Successfully processed delivery picked up event: orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to process delivery picked up event: orderId={}", event.orderId(), e);
            // 재처리를 위해 acknowledge하지 않음
            throw e;
        }
    }

    /**
     * 배달 완료 이벤트 수신
     */
    @KafkaListener(
            topics = "delivery.completed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryCompleted(
            @Payload DeliveryCompletedEvent event,
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

    // Event DTOs
    public record DeliveryPickedUpEvent(
            String orderId,
            String deliveryId,
            String riderId
    ) {}

    public record DeliveryCompletedEvent(
            String orderId,
            String deliveryId,
            String riderId,
            java.time.LocalDateTime completedAt
    ) {}
}

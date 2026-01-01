package jjh.delivery.domain.shipment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Shipment Aggregate Root
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class Shipment {

    private final String id;
    private final String orderId;
    private ShippingCarrier carrier;
    private String trackingNumber;
    private ShipmentStatus status;
    private final List<TrackingEvent> trackingEvents;
    private LocalDateTime estimatedDeliveryDate;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    private Shipment(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.orderId = builder.orderId;
        this.carrier = builder.carrier;
        this.trackingNumber = builder.trackingNumber;
        this.status = builder.status != null ? builder.status : ShipmentStatus.PENDING;
        this.trackingEvents = new ArrayList<>(builder.trackingEvents);
        this.estimatedDeliveryDate = builder.estimatedDeliveryDate;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.shippedAt = builder.shippedAt;
        this.deliveredAt = builder.deliveredAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 운송장 등록
     */
    public void registerTracking(ShippingCarrier carrier, String trackingNumber) {
        if (this.status != ShipmentStatus.PENDING) {
            throw new IllegalStateException("Can only register tracking in PENDING status");
        }
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 집화 완료
     */
    public void pickUp(String location) {
        validateStatusTransition(ShipmentStatus.PICKED_UP);
        this.status = ShipmentStatus.PICKED_UP;
        this.shippedAt = LocalDateTime.now();
        addTrackingEvent(ShipmentStatus.PICKED_UP, location, "상품이 집하되었습니다");
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 배송중 (허브 이동)
     */
    public void inTransit(String location, String description) {
        validateStatusTransition(ShipmentStatus.IN_TRANSIT);
        this.status = ShipmentStatus.IN_TRANSIT;
        addTrackingEvent(ShipmentStatus.IN_TRANSIT, location, description);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 배달중 (최종 배송)
     */
    public void outForDelivery(String location) {
        validateStatusTransition(ShipmentStatus.OUT_FOR_DELIVERY);
        this.status = ShipmentStatus.OUT_FOR_DELIVERY;
        addTrackingEvent(ShipmentStatus.OUT_FOR_DELIVERY, location, "배달을 시작합니다");
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 배송 완료
     */
    public void deliver(String location) {
        validateStatusTransition(ShipmentStatus.DELIVERED);
        this.status = ShipmentStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        addTrackingEvent(ShipmentStatus.DELIVERED, location, "배송이 완료되었습니다");
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 배송 시도 실패
     */
    public void failDeliveryAttempt(String location, String reason) {
        validateStatusTransition(ShipmentStatus.FAILED_ATTEMPT);
        this.status = ShipmentStatus.FAILED_ATTEMPT;
        addTrackingEvent(ShipmentStatus.FAILED_ATTEMPT, location, "배송 시도 실패: " + reason);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 배송 재시도
     */
    public void retryDelivery(String location) {
        if (this.status != ShipmentStatus.FAILED_ATTEMPT) {
            throw new IllegalStateException("Can only retry from FAILED_ATTEMPT status");
        }
        this.status = ShipmentStatus.OUT_FOR_DELIVERY;
        addTrackingEvent(ShipmentStatus.OUT_FOR_DELIVERY, location, "배송을 재시도합니다");
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 반송
     */
    public void returnToSender(String location, String reason) {
        if (this.status != ShipmentStatus.FAILED_ATTEMPT) {
            throw new IllegalStateException("Can only return from FAILED_ATTEMPT status");
        }
        this.status = ShipmentStatus.RETURNED;
        addTrackingEvent(ShipmentStatus.RETURNED, location, "반송 처리: " + reason);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 취소
     */
    public void cancel(String reason) {
        if (!status.isCancellable()) {
            throw new IllegalStateException("Cannot cancel shipment in status: " + status);
        }
        this.status = ShipmentStatus.CANCELLED;
        addTrackingEvent(ShipmentStatus.CANCELLED, null, "배송 취소: " + reason);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 예상 배송일 설정
     */
    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 추적 이벤트 추가
     */
    public void addTrackingEvent(ShipmentStatus status, String location, String description) {
        trackingEvents.add(TrackingEvent.of(status, location, description));
    }

    // =====================================================
    // Private Methods
    // =====================================================

    private void validateStatusTransition(ShipmentStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from " + status + " to " + newStatus);
        }
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 운송장 조회 URL
     */
    public String getTrackingUrl() {
        if (carrier == null || trackingNumber == null) {
            return null;
        }
        return carrier.getTrackingUrl(trackingNumber);
    }

    /**
     * 배송 완료 여부
     */
    public boolean isDelivered() {
        return status == ShipmentStatus.DELIVERED;
    }

    /**
     * 배송중 여부
     */
    public boolean isInProgress() {
        return status.isInProgress();
    }

    /**
     * 최신 추적 이벤트
     */
    public TrackingEvent getLatestTrackingEvent() {
        if (trackingEvents.isEmpty()) {
            return null;
        }
        return trackingEvents.get(trackingEvents.size() - 1);
    }

    // =====================================================
    // Getters
    // =====================================================

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public ShippingCarrier getCarrier() {
        return carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public List<TrackingEvent> getTrackingEvents() {
        return Collections.unmodifiableList(trackingEvents);
    }

    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private String id;
        private String orderId;
        private ShippingCarrier carrier;
        private String trackingNumber;
        private ShipmentStatus status;
        private List<TrackingEvent> trackingEvents = new ArrayList<>();
        private LocalDateTime estimatedDeliveryDate;
        private LocalDateTime createdAt;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder carrier(ShippingCarrier carrier) {
            this.carrier = carrier;
            return this;
        }

        public Builder trackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
            return this;
        }

        public Builder status(ShipmentStatus status) {
            this.status = status;
            return this;
        }

        public Builder trackingEvents(List<TrackingEvent> trackingEvents) {
            this.trackingEvents = new ArrayList<>(trackingEvents);
            return this;
        }

        public Builder estimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
            this.estimatedDeliveryDate = estimatedDeliveryDate;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder shippedAt(LocalDateTime shippedAt) {
            this.shippedAt = shippedAt;
            return this;
        }

        public Builder deliveredAt(LocalDateTime deliveredAt) {
            this.deliveredAt = deliveredAt;
            return this;
        }

        public Shipment build() {
            validateRequired();
            return new Shipment(this);
        }

        private void validateRequired() {
            if (orderId == null || orderId.isBlank()) {
                throw new IllegalArgumentException("orderId is required");
            }
        }
    }
}

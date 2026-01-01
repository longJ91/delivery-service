package jjh.delivery.domain.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Order Aggregate Root
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class Order {

    private final String id;
    private final String customerId;
    private final String shopId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final String deliveryAddress;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Order(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.customerId = builder.customerId;
        this.shopId = builder.shopId;
        this.items = new ArrayList<>(builder.items);
        this.status = builder.status != null ? builder.status : OrderStatus.PENDING;
        this.deliveryAddress = builder.deliveryAddress;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Domain Business Logic
    public void accept() {
        validateStatusTransition(OrderStatus.ACCEPTED);
        this.status = OrderStatus.ACCEPTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void startPreparing() {
        validateStatusTransition(OrderStatus.PREPARING);
        this.status = OrderStatus.PREPARING;
        this.updatedAt = LocalDateTime.now();
    }

    public void readyForDelivery() {
        validateStatusTransition(OrderStatus.READY_FOR_DELIVERY);
        this.status = OrderStatus.READY_FOR_DELIVERY;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order in status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal calculateTotalAmount() {
        return items.stream()
                .map(OrderItem::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateStatusTransition(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from " + status + " to " + newStatus);
        }
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getShopId() {
        return shopId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static class Builder {
        private String id;
        private String customerId;
        private String shopId;
        private List<OrderItem> items = new ArrayList<>();
        private OrderStatus status;
        private String deliveryAddress;
        private LocalDateTime createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder shopId(String shopId) {
            this.shopId = shopId;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public Builder addItem(OrderItem item) {
            this.items.add(item);
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder deliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Order build() {
            validateRequired();
            return new Order(this);
        }

        private void validateRequired() {
            if (customerId == null || customerId.isBlank()) {
                throw new IllegalArgumentException("customerId is required");
            }
            if (shopId == null || shopId.isBlank()) {
                throw new IllegalArgumentException("shopId is required");
            }
            if (deliveryAddress == null || deliveryAddress.isBlank()) {
                throw new IllegalArgumentException("deliveryAddress is required");
            }
            if (items.isEmpty()) {
                throw new IllegalArgumentException("Order must have at least one item");
            }
        }
    }
}

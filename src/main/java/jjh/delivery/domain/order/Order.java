package jjh.delivery.domain.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Order Aggregate Root (v2 - Product Delivery)
 *
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class Order {

    private final UUID id;
    private final String orderNumber;
    private final UUID customerId;
    private final UUID sellerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final ShippingAddress shippingAddress;

    // 금액 정보
    private final BigDecimal subtotalAmount;
    private final BigDecimal shippingFee;
    private final BigDecimal discountAmount;
    private final BigDecimal totalAmount;

    // 메모
    private final String orderMemo;
    private final String shippingMemo;

    // 쿠폰 (nullable)
    private final UUID couponId;

    // 타임스탬프
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    private Order(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.orderNumber = builder.orderNumber != null ? builder.orderNumber : generateOrderNumber();
        this.customerId = builder.customerId;
        this.sellerId = builder.sellerId;
        this.items = new ArrayList<>(builder.items);
        this.status = builder.status != null ? builder.status : OrderStatus.PENDING;
        this.shippingAddress = builder.shippingAddress;

        // 금액 계산
        this.subtotalAmount = builder.subtotalAmount != null ? builder.subtotalAmount : calculateSubtotal();
        this.shippingFee = builder.shippingFee != null ? builder.shippingFee : BigDecimal.ZERO;
        this.discountAmount = builder.discountAmount != null ? builder.discountAmount : BigDecimal.ZERO;
        this.totalAmount = builder.totalAmount != null ? builder.totalAmount : calculateTotal();

        this.orderMemo = builder.orderMemo;
        this.shippingMemo = builder.shippingMemo;
        this.couponId = builder.couponId;

        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.paidAt = builder.paidAt;
        this.confirmedAt = builder.confirmedAt;
        this.shippedAt = builder.shippedAt;
        this.deliveredAt = builder.deliveredAt;
        this.cancelledAt = builder.cancelledAt;
    }

    private static String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        return "ORD-" + date + "-" + random;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic - 상태 전이
    // =====================================================

    /**
     * 결제 완료 처리
     */
    public void pay() {
        validateStatusTransition(OrderStatus.PAID);
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 주문 확정 (판매자 확인)
     */
    public void confirm() {
        validateStatusTransition(OrderStatus.CONFIRMED);
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 상품 준비 시작
     */
    public void startPreparing() {
        validateStatusTransition(OrderStatus.PREPARING);
        this.status = OrderStatus.PREPARING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 출고 완료 (배송 시작)
     */
    public void ship() {
        validateStatusTransition(OrderStatus.SHIPPED);
        this.status = OrderStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 배송중 (허브 이동)
     */
    public void inTransit() {
        validateStatusTransition(OrderStatus.IN_TRANSIT);
        this.status = OrderStatus.IN_TRANSIT;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 배달중 (최종 배송)
     */
    public void outForDelivery() {
        validateStatusTransition(OrderStatus.OUT_FOR_DELIVERY);
        this.status = OrderStatus.OUT_FOR_DELIVERY;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 배송 완료
     */
    public void deliver() {
        validateStatusTransition(OrderStatus.DELIVERED);
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 주문 취소
     */
    public void cancel() {
        if (!status.isCancellable()) {
            throw new IllegalStateException("Cannot cancel order in status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 반품 요청
     */
    public void requestReturn() {
        if (!status.isReturnable()) {
            throw new IllegalStateException("Cannot request return in status: " + status);
        }
        validateStatusTransition(OrderStatus.RETURN_REQUESTED);
        this.status = OrderStatus.RETURN_REQUESTED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 반품 완료
     */
    public void completeReturn() {
        validateStatusTransition(OrderStatus.RETURNED);
        this.status = OrderStatus.RETURNED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 반품 거절 (배송 완료로 복원)
     */
    public void rejectReturn() {
        if (this.status != OrderStatus.RETURN_REQUESTED) {
            throw new IllegalStateException("Can only reject return from RETURN_REQUESTED status");
        }
        this.status = OrderStatus.DELIVERED;
        this.updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // 금액 계산
    // =====================================================

    private BigDecimal calculateSubtotal() {
        return items.stream()
                .map(OrderItem::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotal() {
        return subtotalAmount
                .add(shippingFee)
                .subtract(discountAmount);
    }

    /**
     * 총 주문 금액 반환 (외부용)
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 주문 아이템 수 반환
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * 총 상품 수량 반환
     */
    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(OrderItem::quantity)
                .sum();
    }

    /**
     * 특정 상품 포함 여부 확인
     */
    public boolean containsProduct(UUID productId) {
        return items.stream()
                .anyMatch(item -> item.productId().equals(productId));
    }

    // =====================================================
    // Validation
    // =====================================================

    private void validateStatusTransition(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from " + status + " to " + newStatus);
        }
    }

    // =====================================================
    // Getters
    // =====================================================

    public UUID getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public String getOrderMemo() {
        return orderMemo;
    }

    public String getShippingMemo() {
        return shippingMemo;
    }

    public UUID getCouponId() {
        return couponId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private UUID id;
        private String orderNumber;
        private UUID customerId;
        private UUID sellerId;
        private List<OrderItem> items = new ArrayList<>();
        private OrderStatus status;
        private ShippingAddress shippingAddress;

        private BigDecimal subtotalAmount;
        private BigDecimal shippingFee;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;

        private String orderMemo;
        private String shippingMemo;
        private UUID couponId;

        private LocalDateTime createdAt;
        private LocalDateTime paidAt;
        private LocalDateTime confirmedAt;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime cancelledAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder orderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        public Builder customerId(UUID customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder sellerId(UUID sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = new ArrayList<>(items);
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

        public Builder shippingAddress(ShippingAddress shippingAddress) {
            this.shippingAddress = shippingAddress;
            return this;
        }

        public Builder subtotalAmount(BigDecimal subtotalAmount) {
            this.subtotalAmount = subtotalAmount;
            return this;
        }

        public Builder shippingFee(BigDecimal shippingFee) {
            this.shippingFee = shippingFee;
            return this;
        }

        public Builder discountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder orderMemo(String orderMemo) {
            this.orderMemo = orderMemo;
            return this;
        }

        public Builder shippingMemo(String shippingMemo) {
            this.shippingMemo = shippingMemo;
            return this;
        }

        public Builder couponId(UUID couponId) {
            this.couponId = couponId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder paidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
            return this;
        }

        public Builder confirmedAt(LocalDateTime confirmedAt) {
            this.confirmedAt = confirmedAt;
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

        public Builder cancelledAt(LocalDateTime cancelledAt) {
            this.cancelledAt = cancelledAt;
            return this;
        }

        public Order build() {
            validateRequired();
            return new Order(this);
        }

        private void validateRequired() {
            if (customerId == null) {
                throw new IllegalArgumentException("customerId is required");
            }
            if (sellerId == null) {
                throw new IllegalArgumentException("sellerId is required");
            }
            if (shippingAddress == null) {
                throw new IllegalArgumentException("shippingAddress is required");
            }
            if (items.isEmpty()) {
                throw new IllegalArgumentException("Order must have at least one item");
            }
        }
    }
}

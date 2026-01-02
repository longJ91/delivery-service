package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import jjh.delivery.domain.order.OrderStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order JPA Entity (v2 - Product Delivery)
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
        @Index(name = "idx_orders_seller_id", columnList = "seller_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "recipientName", column = @Column(name = "shipping_recipient_name")),
            @AttributeOverride(name = "phoneNumber", column = @Column(name = "shipping_phone_number")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code")),
            @AttributeOverride(name = "address1", column = @Column(name = "shipping_address1")),
            @AttributeOverride(name = "address2", column = @Column(name = "shipping_address2")),
            @AttributeOverride(name = "deliveryNote", column = @Column(name = "shipping_delivery_note"))
    })
    private ShippingAddressEmbeddable shippingAddress;

    // 금액 정보
    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "shipping_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // 메모
    @Column(name = "order_memo", length = 500)
    private String orderMemo;

    @Column(name = "shipping_memo", length = 500)
    private String shippingMemo;

    // 쿠폰 (nullable)
    @Column(name = "coupon_id", length = 36)
    private String couponId;

    // 타임스탬프
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Version
    private Long version;

    @Builder
    public OrderJpaEntity(
            String id,
            String orderNumber,
            String customerId,
            String sellerId,
            OrderStatus status,
            ShippingAddressEmbeddable shippingAddress,
            BigDecimal subtotalAmount,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            String orderMemo,
            String shippingMemo,
            String couponId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime paidAt,
            LocalDateTime confirmedAt,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt,
            LocalDateTime cancelledAt
    ) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.sellerId = sellerId;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.subtotalAmount = subtotalAmount;
        this.shippingFee = shippingFee;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.orderMemo = orderMemo;
        this.shippingMemo = shippingMemo;
        this.couponId = couponId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.paidAt = paidAt;
        this.confirmedAt = confirmedAt;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
        this.cancelledAt = cancelledAt;
    }

    public void addItem(OrderItemJpaEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    public void clearItems() {
        items.clear();
    }

    // Setters for update
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}

package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.OrderItemJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.ShippingAddressEmbeddable;
import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Order Persistence Mapper (v2 - Product Delivery)
 * Domain <-> JPA Entity 변환
 */
@Component
public class OrderPersistenceMapper {

    public OrderJpaEntity toEntity(Order order) {
        ShippingAddressEmbeddable shippingAddressEntity =
                ShippingAddressEmbeddable.fromDomain(order.getShippingAddress());

        OrderJpaEntity entity = new OrderJpaEntity(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getSellerId(),
                order.getStatus(),
                shippingAddressEntity,
                order.getSubtotalAmount(),
                order.getShippingFee(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getOrderMemo(),
                order.getShippingMemo(),
                order.getCouponId(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getPaidAt(),
                order.getConfirmedAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                order.getCancelledAt()
        );

        order.getItems().forEach(item -> {
            OrderItemJpaEntity itemEntity = toItemEntity(item);
            entity.addItem(itemEntity);
        });

        return entity;
    }

    private OrderItemJpaEntity toItemEntity(OrderItem item) {
        return new OrderItemJpaEntity(
                item.productId(),
                item.productName(),
                item.variantId(),
                item.variantName(),
                item.sku(),
                item.optionValues(),
                item.quantity(),
                item.unitPrice()
        );
    }

    public Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(this::toDomainItem)
                .toList();

        return Order.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .customerId(entity.getCustomerId())
                .sellerId(entity.getSellerId())
                .items(items)
                .status(entity.getStatus())
                .shippingAddress(entity.getShippingAddress().toDomain())
                .subtotalAmount(entity.getSubtotalAmount())
                .shippingFee(entity.getShippingFee())
                .discountAmount(entity.getDiscountAmount())
                .totalAmount(entity.getTotalAmount())
                .orderMemo(entity.getOrderMemo())
                .shippingMemo(entity.getShippingMemo())
                .couponId(entity.getCouponId())
                .createdAt(entity.getCreatedAt())
                .paidAt(entity.getPaidAt())
                .confirmedAt(entity.getConfirmedAt())
                .shippedAt(entity.getShippedAt())
                .deliveredAt(entity.getDeliveredAt())
                .cancelledAt(entity.getCancelledAt())
                .build();
    }

    private OrderItem toDomainItem(OrderItemJpaEntity entity) {
        if (entity.hasVariant()) {
            return OrderItem.ofVariant(
                    entity.getProductId(),
                    entity.getProductName(),
                    entity.getVariantId(),
                    entity.getVariantName(),
                    entity.getSku(),
                    entity.getOptionValues(),
                    entity.getQuantity(),
                    entity.getUnitPrice()
            );
        } else {
            return OrderItem.of(
                    entity.getProductId(),
                    entity.getProductName(),
                    entity.getQuantity(),
                    entity.getUnitPrice()
            );
        }
    }

    public List<Order> toDomainList(List<OrderJpaEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .toList();
    }
}

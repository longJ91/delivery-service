package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.OrderItemJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Order Persistence Mapper
 * Domain <-> JPA Entity 변환
 */
@Component
public class OrderPersistenceMapper {

    public OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity(
                order.getId(),
                order.getCustomerId(),
                order.getShopId(),
                order.getStatus(),
                order.calculateTotalAmount(),
                order.getDeliveryAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );

        order.getItems().forEach(item -> {
            OrderItemJpaEntity itemEntity = new OrderItemJpaEntity(
                    item.menuId(),
                    item.menuName(),
                    item.quantity(),
                    item.unitPrice()
            );
            entity.addItem(itemEntity);
        });

        return entity;
    }

    public Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(this::toDomainItem)
                .toList();

        return Order.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .shopId(entity.getShopId())
                .items(items)
                .status(entity.getStatus())
                .deliveryAddress(entity.getDeliveryAddress())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private OrderItem toDomainItem(OrderItemJpaEntity entity) {
        return new OrderItem(
                entity.getMenuId(),
                entity.getMenuName(),
                entity.getQuantity(),
                entity.getUnitPrice()
        );
    }

    public List<Order> toDomainList(List<OrderJpaEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .toList();
    }
}

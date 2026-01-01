package jjh.delivery.adapter.in.web.mapper;

import jjh.delivery.adapter.in.web.dto.CreateOrderRequest;
import jjh.delivery.adapter.in.web.dto.OrderResponse;
import jjh.delivery.adapter.in.web.dto.OrderResponse.OrderItemResponse;
import jjh.delivery.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import jjh.delivery.application.port.in.CreateOrderUseCase.OrderItemCommand;
import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Order Web Mapper
 * Web DTO <-> Application Command/Domain 변환
 */
@Component
public class OrderWebMapper {

    public CreateOrderCommand toCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(
                request.customerId(),
                request.shopId(),
                request.items().stream()
                        .map(this::toItemCommand)
                        .toList(),
                request.deliveryAddress()
        );
    }

    private OrderItemCommand toItemCommand(CreateOrderRequest.OrderItemRequest item) {
        return new OrderItemCommand(
                item.menuId(),
                item.menuName(),
                item.quantity(),
                item.unitPrice()
        );
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getShopId(),
                order.getItems().stream()
                        .map(this::toItemResponse)
                        .toList(),
                order.getStatus(),
                order.calculateTotalAmount(),
                order.getDeliveryAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.menuId(),
                item.menuName(),
                item.quantity(),
                item.unitPrice(),
                item.calculateSubtotal()
        );
    }

    public List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponse)
                .toList();
    }
}

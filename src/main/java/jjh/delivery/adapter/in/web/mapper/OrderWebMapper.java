package jjh.delivery.adapter.in.web.mapper;

import jjh.delivery.adapter.in.web.dto.CreateOrderRequest;
import jjh.delivery.adapter.in.web.dto.CreateOrderRequest.OrderItemRequest;
import jjh.delivery.adapter.in.web.dto.CreateOrderRequest.ShippingAddressRequest;
import jjh.delivery.adapter.in.web.dto.OrderResponse;
import jjh.delivery.adapter.in.web.dto.OrderResponse.OrderItemResponse;
import jjh.delivery.adapter.in.web.dto.OrderResponse.ShippingAddressResponse;
import jjh.delivery.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import jjh.delivery.application.port.in.CreateOrderUseCase.OrderItemCommand;
import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderItem;
import jjh.delivery.domain.order.ShippingAddress;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Order Web Mapper (v2 - Product Delivery)
 * Web DTO <-> Application Command/Domain 변환
 */
@Component
public class OrderWebMapper {

    public CreateOrderCommand toCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(
                request.customerId(),
                request.sellerId(),
                request.items().stream()
                        .map(this::toItemCommand)
                        .toList(),
                toShippingAddress(request.shippingAddress()),
                request.orderMemo(),
                request.shippingMemo(),
                request.couponId()
        );
    }

    private OrderItemCommand toItemCommand(OrderItemRequest item) {
        if (item.variantId() != null && !item.variantId().isBlank()) {
            return OrderItemCommand.ofVariant(
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
        return OrderItemCommand.of(
                item.productId(),
                item.productName(),
                item.quantity(),
                item.unitPrice()
        );
    }

    private ShippingAddress toShippingAddress(ShippingAddressRequest request) {
        return ShippingAddress.of(
                request.recipientName(),
                request.phoneNumber(),
                request.postalCode(),
                request.address1(),
                request.address2(),
                request.deliveryNote()
        );
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getSellerId(),
                order.getItems().stream()
                        .map(this::toItemResponse)
                        .toList(),
                order.getStatus(),
                toShippingAddressResponse(order.getShippingAddress()),
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
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.productId(),
                item.productName(),
                item.variantId(),
                item.variantName(),
                item.sku(),
                item.optionValues(),
                item.quantity(),
                item.unitPrice(),
                item.calculateSubtotal()
        );
    }

    private ShippingAddressResponse toShippingAddressResponse(ShippingAddress address) {
        if (address == null) {
            return null;
        }
        return new ShippingAddressResponse(
                address.recipientName(),
                address.phoneNumber(),
                address.postalCode(),
                address.address1(),
                address.address2(),
                address.deliveryNote(),
                address.getFullAddress()
        );
    }

    public List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponse)
                .toList();
    }
}

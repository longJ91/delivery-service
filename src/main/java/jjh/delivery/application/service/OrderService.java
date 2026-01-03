package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;

import jjh.delivery.application.port.in.CreateOrderUseCase;
import jjh.delivery.application.port.in.GetOrderUseCase;
import jjh.delivery.application.port.in.SearchOrderUseCase;
import jjh.delivery.application.port.in.UpdateOrderStatusUseCase;
import jjh.delivery.application.port.out.LoadOrderPort;
import jjh.delivery.application.port.out.OrderEventPort;
import jjh.delivery.application.port.out.OrderSearchPort;
import jjh.delivery.application.port.out.SaveOrderPort;
import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderItem;
import jjh.delivery.domain.order.OrderStatus;
import jjh.delivery.domain.order.event.OrderCreatedEvent;
import jjh.delivery.domain.order.event.OrderStatusChangedEvent;
import jjh.delivery.domain.order.exception.OrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Order Application Service (v2 - Product Delivery)
 * Use Case 구현체 - 비즈니스 흐름 조합 (도메인 로직은 Domain 객체에 위임)
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService implements CreateOrderUseCase, GetOrderUseCase,
        UpdateOrderStatusUseCase, SearchOrderUseCase {

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
    private final OrderEventPort orderEventPort;
    private final OrderSearchPort orderSearchPort;

    // ==================== CreateOrderUseCase ====================

    @Override
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        Order order = Order.builder()
                .customerId(UUID.fromString(command.customerId()))
                .sellerId(UUID.fromString(command.sellerId()))
                .items(toOrderItems(command.items()))
                .shippingAddress(command.shippingAddress())
                .orderMemo(command.orderMemo())
                .shippingMemo(command.shippingMemo())
                .couponId(command.couponId() != null ? UUID.fromString(command.couponId()) : null)
                .build();

        Order savedOrder = saveOrderPort.save(order);

        // Elasticsearch 인덱싱
        orderSearchPort.index(savedOrder);

        // 이벤트 발행
        orderEventPort.publishAsync(OrderCreatedEvent.from(savedOrder));

        return savedOrder;
    }

    private List<OrderItem> toOrderItems(List<OrderItemCommand> commands) {
        return commands.stream()
                .map(cmd -> {
                    UUID productId = UUID.fromString(cmd.productId());
                    if (cmd.variantId() != null) {
                        return OrderItem.ofVariant(
                                productId,
                                cmd.productName(),
                                UUID.fromString(cmd.variantId()),
                                cmd.variantName(),
                                cmd.sku(),
                                cmd.optionValues(),
                                cmd.quantity(),
                                cmd.unitPrice()
                        );
                    } else {
                        return OrderItem.of(
                                productId,
                                cmd.productName(),
                                cmd.quantity(),
                                cmd.unitPrice()
                        );
                    }
                })
                .toList();
    }

    // ==================== GetOrderUseCase ====================

    @Override
    public Optional<Order> getOrder(UUID orderId) {
        return loadOrderPort.findById(orderId);
    }

    @Override
    public Order getOrderOrThrow(UUID orderId) {
        return loadOrderPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
    }

    // ==================== UpdateOrderStatusUseCase ====================

    @Override
    @Transactional
    public Order payOrder(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus previousStatus = order.getStatus();

        order.pay();

        return saveAndPublishStatusChange(order, previousStatus);
    }

    @Override
    @Transactional
    public Order confirmOrder(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus previousStatus = order.getStatus();

        order.confirm();

        return saveAndPublishStatusChange(order, previousStatus);
    }

    @Override
    @Transactional
    public Order startPreparing(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus previousStatus = order.getStatus();

        order.startPreparing();

        return saveAndPublishStatusChange(order, previousStatus);
    }

    @Override
    @Transactional
    public Order shipOrder(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus previousStatus = order.getStatus();

        order.ship();

        return saveAndPublishStatusChange(order, previousStatus);
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus previousStatus = order.getStatus();

        order.cancel();

        return saveAndPublishStatusChange(order, previousStatus);
    }

    @Override
    @Transactional
    public Order requestReturn(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus previousStatus = order.getStatus();

        order.requestReturn();

        return saveAndPublishStatusChange(order, previousStatus);
    }

    @Override
    @Transactional
    public Order updateStatus(UUID orderId, OrderStatus newStatus) {
        return switch (newStatus) {
            case PAID -> payOrder(orderId);
            case CONFIRMED -> confirmOrder(orderId);
            case PREPARING -> startPreparing(orderId);
            case SHIPPED -> shipOrder(orderId);
            case CANCELLED -> cancelOrder(orderId);
            case RETURN_REQUESTED -> requestReturn(orderId);
            default -> throw new IllegalArgumentException(
                    "Cannot directly update to status: " + newStatus);
        };
    }

    private Order saveAndPublishStatusChange(Order order, OrderStatus previousStatus) {
        Order savedOrder = saveOrderPort.save(order);
        orderSearchPort.index(savedOrder);
        orderEventPort.publishAsync(OrderStatusChangedEvent.of(savedOrder, previousStatus));
        return savedOrder;
    }

    // ==================== SearchOrderUseCase ====================

    @Override
    public List<Order> searchOrders(SearchOrderQuery query) {
        return orderSearchPort.search(query);
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return orderSearchPort.findByCustomerId(customerId);
    }

    @Override
    public List<Order> findBySellerId(UUID sellerId) {
        return orderSearchPort.findBySellerId(sellerId);
    }
}

package jjh.delivery.application.service;

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

/**
 * Order Application Service
 * Use Case 구현체 - 비즈니스 흐름 조합 (도메인 로직은 Domain 객체에 위임)
 */
@Service
@Transactional(readOnly = true)
public class OrderService implements CreateOrderUseCase, GetOrderUseCase,
        UpdateOrderStatusUseCase, SearchOrderUseCase {

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
    private final OrderEventPort orderEventPort;
    private final OrderSearchPort orderSearchPort;

    public OrderService(
            LoadOrderPort loadOrderPort,
            SaveOrderPort saveOrderPort,
            OrderEventPort orderEventPort,
            OrderSearchPort orderSearchPort
    ) {
        this.loadOrderPort = loadOrderPort;
        this.saveOrderPort = saveOrderPort;
        this.orderEventPort = orderEventPort;
        this.orderSearchPort = orderSearchPort;
    }

    // ==================== CreateOrderUseCase ====================

    @Override
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        Order order = Order.builder()
                .customerId(command.customerId())
                .shopId(command.shopId())
                .items(toOrderItems(command.items()))
                .deliveryAddress(command.deliveryAddress())
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
                .map(cmd -> new OrderItem(
                        cmd.menuId(),
                        cmd.menuName(),
                        cmd.quantity(),
                        cmd.unitPrice()
                ))
                .toList();
    }

    // ==================== GetOrderUseCase ====================

    @Override
    public Optional<Order> getOrder(String orderId) {
        return loadOrderPort.findById(orderId);
    }

    @Override
    public Order getOrderOrThrow(String orderId) {
        return loadOrderPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    // ==================== UpdateOrderStatusUseCase ====================

    @Override
    @Transactional
    public Order acceptOrder(String orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus previousStatus = order.getStatus();

        order.accept();

        return saveAndPublishStatusChange(order, previousStatus);
    }

    @Override
    @Transactional
    public Order startPreparing(String orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus previousStatus = order.getStatus();

        order.startPreparing();

        return saveAndPublishStatusChange(order, previousStatus);
    }

    @Override
    @Transactional
    public Order readyForDelivery(String orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus previousStatus = order.getStatus();

        order.readyForDelivery();

        return saveAndPublishStatusChange(order, previousStatus);
    }

    @Override
    @Transactional
    public Order cancelOrder(String orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus previousStatus = order.getStatus();

        order.cancel();

        return saveAndPublishStatusChange(order, previousStatus);
    }

    @Override
    @Transactional
    public Order updateStatus(String orderId, OrderStatus newStatus) {
        return switch (newStatus) {
            case ACCEPTED -> acceptOrder(orderId);
            case PREPARING -> startPreparing(orderId);
            case READY_FOR_DELIVERY -> readyForDelivery(orderId);
            case CANCELLED -> cancelOrder(orderId);
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
    public List<Order> findByCustomerId(String customerId) {
        return orderSearchPort.findByCustomerId(customerId);
    }

    @Override
    public List<Order> findByShopId(String shopId) {
        return orderSearchPort.findByShopId(shopId);
    }
}

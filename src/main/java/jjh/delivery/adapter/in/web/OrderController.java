package jjh.delivery.adapter.in.web;

import jakarta.validation.Valid;
import jjh.delivery.adapter.in.web.dto.CreateOrderRequest;
import jjh.delivery.adapter.in.web.dto.OrderResponse;
import jjh.delivery.adapter.in.web.dto.UpdateOrderStatusRequest;
import jjh.delivery.adapter.in.web.mapper.OrderWebMapper;
import jjh.delivery.application.port.in.CreateOrderUseCase;
import jjh.delivery.application.port.in.GetOrderUseCase;
import jjh.delivery.application.port.in.SearchOrderUseCase;
import jjh.delivery.application.port.in.UpdateOrderStatusUseCase;
import jjh.delivery.domain.order.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Order REST Controller - Driving Adapter (Inbound)
 * v2 - Product Delivery
 */
@RestController
@RequestMapping("/api/v2/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final SearchOrderUseCase searchOrderUseCase;
    private final OrderWebMapper mapper;

    public OrderController(
            CreateOrderUseCase createOrderUseCase,
            GetOrderUseCase getOrderUseCase,
            UpdateOrderStatusUseCase updateOrderStatusUseCase,
            SearchOrderUseCase searchOrderUseCase,
            OrderWebMapper mapper
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.searchOrderUseCase = searchOrderUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = createOrderUseCase.createOrder(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        Order order = getOrderUseCase.getOrderOrThrow(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        Order order = updateOrderStatusUseCase.updateStatus(orderId, request.status());
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> payOrder(@PathVariable String orderId) {
        Order order = updateOrderStatusUseCase.payOrder(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable String orderId) {
        Order order = updateOrderStatusUseCase.confirmOrder(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/prepare")
    public ResponseEntity<OrderResponse> startPreparing(@PathVariable String orderId) {
        Order order = updateOrderStatusUseCase.startPreparing(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<OrderResponse> shipOrder(@PathVariable String orderId) {
        Order order = updateOrderStatusUseCase.shipOrder(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId) {
        Order order = updateOrderStatusUseCase.cancelOrder(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<OrderResponse> requestReturn(@PathVariable String orderId) {
        Order order = updateOrderStatusUseCase.requestReturn(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable String customerId) {
        List<Order> orders = searchOrderUseCase.findByCustomerId(customerId);
        return ResponseEntity.ok(mapper.toResponseList(orders));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersBySeller(@PathVariable String sellerId) {
        List<Order> orders = searchOrderUseCase.findBySellerId(sellerId);
        return ResponseEntity.ok(mapper.toResponseList(orders));
    }
}

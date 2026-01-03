package jjh.delivery.adapter.in.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.dto.CreateOrderRequest;
import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.adapter.in.web.dto.OrderListResponse;
import jjh.delivery.adapter.in.web.dto.OrderResponse;
import jjh.delivery.adapter.in.web.dto.UpdateOrderStatusRequest;
import jjh.delivery.adapter.in.web.mapper.OrderWebMapper;
import jjh.delivery.application.port.in.CreateOrderUseCase;
import jjh.delivery.application.port.in.GetOrderUseCase;
import jjh.delivery.application.port.in.SearchOrderUseCase;
import jjh.delivery.application.port.in.UpdateOrderStatusUseCase;
import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Order REST Controller - Driving Adapter (Inbound)
 * v2 - Product Delivery
 */
@RestController
@RequestMapping("/api/v2/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final SearchOrderUseCase searchOrderUseCase;
    private final OrderWebMapper mapper;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = createOrderUseCase.createOrder(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(order));
    }

    /**
     * 내 주문 목록 조회 (커서 기반 페이지네이션)
     *
     * @param cursor 이전 페이지의 nextCursor 값 (첫 페이지는 생략)
     * @param size   조회할 주문 수 (기본값: 20)
     */
    @GetMapping
    public ResponseEntity<OrderListResponse> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID customerId = UUID.fromString(userDetails.getUsername());

        var queryBuilder = SearchOrderUseCase.SearchOrderQuery.builder()
                .customerId(customerId.toString())
                .cursor(cursor)
                .size(size);

        if (status != null && !status.isBlank()) {
            queryBuilder.status(OrderStatus.valueOf(status));
        }

        CursorPageResponse<Order> result = searchOrderUseCase.searchOrders(queryBuilder.build());

        return ResponseEntity.ok(OrderListResponse.from(result));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        Order order = getOrderUseCase.getOrderOrThrow(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        Order order = updateOrderStatusUseCase.updateStatus(orderId, request.status());
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> payOrder(@PathVariable UUID orderId) {
        Order order = updateOrderStatusUseCase.payOrder(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable UUID orderId) {
        Order order = updateOrderStatusUseCase.confirmOrder(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/prepare")
    public ResponseEntity<OrderResponse> startPreparing(@PathVariable UUID orderId) {
        Order order = updateOrderStatusUseCase.startPreparing(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<OrderResponse> shipOrder(@PathVariable UUID orderId) {
        Order order = updateOrderStatusUseCase.shipOrder(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        Order order = updateOrderStatusUseCase.cancelOrder(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<OrderResponse> requestReturn(@PathVariable UUID orderId) {
        Order order = updateOrderStatusUseCase.requestReturn(orderId);
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable UUID customerId) {
        List<Order> orders = searchOrderUseCase.findByCustomerId(customerId);
        return ResponseEntity.ok(mapper.toResponseList(orders));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersBySeller(@PathVariable UUID sellerId) {
        List<Order> orders = searchOrderUseCase.findBySellerId(sellerId);
        return ResponseEntity.ok(mapper.toResponseList(orders));
    }
}

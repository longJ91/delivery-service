package jjh.delivery.application.service;

import jjh.delivery.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import jjh.delivery.application.port.in.CreateOrderUseCase.OrderItemCommand;
import jjh.delivery.application.port.in.SearchOrderUseCase.SearchOrderQuery;
import jjh.delivery.application.port.out.LoadOrderPort;
import jjh.delivery.application.port.out.OrderEventPort;
import jjh.delivery.application.port.out.OrderSearchPort;
import jjh.delivery.application.port.out.SaveOrderPort;
import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderItem;
import jjh.delivery.domain.order.OrderStatus;
import jjh.delivery.domain.order.ShippingAddress;
import jjh.delivery.domain.order.event.OrderCreatedEvent;
import jjh.delivery.domain.order.event.OrderStatusChangedEvent;
import jjh.delivery.domain.order.exception.OrderNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * OrderService Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 테스트")
class OrderServiceTest {

    @Mock
    private LoadOrderPort loadOrderPort;

    @Mock
    private SaveOrderPort saveOrderPort;

    @Mock
    private OrderEventPort orderEventPort;

    @Mock
    private OrderSearchPort orderSearchPort;

    @InjectMocks
    private OrderService orderService;

    // =====================================================
    // Test Fixtures
    // =====================================================

    private static final String ORDER_ID = "order-123";
    private static final String CUSTOMER_ID = "customer-456";
    private static final String SELLER_ID = "seller-789";

    private Order createOrder() {
        return Order.builder()
                .customerId(CUSTOMER_ID)
                .sellerId(SELLER_ID)
                .shippingAddress(ShippingAddress.of(
                        "홍길동", "010-1234-5678", "12345",
                        "서울시 강남구", "상세주소", null
                ))
                .addItem(OrderItem.of("product-1", "테스트 상품", 2, new BigDecimal("10000")))
                .build();
    }

    private Order createPaidOrder() {
        Order order = createOrder();
        order.pay();
        return order;
    }

    private Order createConfirmedOrder() {
        Order order = createPaidOrder();
        order.confirm();
        return order;
    }

    private Order createPreparingOrder() {
        Order order = createConfirmedOrder();
        order.startPreparing();
        return order;
    }

    private Order createShippedOrder() {
        Order order = createPreparingOrder();
        order.ship();
        return order;
    }

    private Order createInTransitOrder() {
        Order order = createShippedOrder();
        order.inTransit();
        return order;
    }

    private Order createOutForDeliveryOrder() {
        Order order = createInTransitOrder();
        order.outForDelivery();
        return order;
    }

    private Order createDeliveredOrder() {
        Order order = createOutForDeliveryOrder();
        order.deliver();
        return order;
    }

    private CreateOrderCommand createOrderCommand() {
        return new CreateOrderCommand(
                CUSTOMER_ID,
                SELLER_ID,
                List.of(OrderItemCommand.of("product-1", "테스트 상품", 2, new BigDecimal("10000"))),
                ShippingAddress.of("홍길동", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null),
                "주문 메모",
                "배송 메모",
                null
        );
    }

    // =====================================================
    // 주문 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("주문 생성 성공")
        void createOrderSuccess() {
            // given
            CreateOrderCommand command = createOrderCommand();

            given(saveOrderPort.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Order result = orderService.createOrder(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(result.getSellerId()).isEqualTo(SELLER_ID);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.getItems()).hasSize(1);

            verify(saveOrderPort).save(any(Order.class));
            verify(orderSearchPort).index(any(Order.class));
            verify(orderEventPort).publishAsync(any(OrderCreatedEvent.class));
        }

        @Test
        @DisplayName("변형 상품 포함 주문 생성")
        void createOrderWithVariantSuccess() {
            // given
            CreateOrderCommand command = new CreateOrderCommand(
                    CUSTOMER_ID,
                    SELLER_ID,
                    List.of(OrderItemCommand.ofVariant(
                            "product-1", "테스트 상품",
                            "variant-1", "빨강/L", "SKU-RED-L",
                            Map.of("색상", "빨강", "사이즈", "L"),
                            1, new BigDecimal("12000")
                    )),
                    ShippingAddress.of("홍길동", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null),
                    null, null, null
            );

            given(saveOrderPort.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Order result = orderService.createOrder(command);

            // then
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).variantId()).isEqualTo("variant-1");
            assertThat(result.getItems().get(0).variantName()).isEqualTo("빨강/L");
        }
    }

    // =====================================================
    // 주문 조회 테스트
    // =====================================================

    @Nested
    @DisplayName("주문 조회")
    class GetOrder {

        @Test
        @DisplayName("주문 조회 성공")
        void getOrderSuccess() {
            // given
            Order order = createOrder();

            given(loadOrderPort.findById(order.getId()))
                    .willReturn(Optional.of(order));

            // when
            Optional<Order> result = orderService.getOrder(order.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(order);
        }

        @Test
        @DisplayName("주문 없으면 빈 Optional 반환")
        void getOrderNotFoundReturnsEmpty() {
            // given
            given(loadOrderPort.findById("non-existent"))
                    .willReturn(Optional.empty());

            // when
            Optional<Order> result = orderService.getOrder("non-existent");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getOrderOrThrow 성공")
        void getOrderOrThrowSuccess() {
            // given
            Order order = createOrder();

            given(loadOrderPort.findById(order.getId()))
                    .willReturn(Optional.of(order));

            // when
            Order result = orderService.getOrderOrThrow(order.getId());

            // then
            assertThat(result).isEqualTo(order);
        }

        @Test
        @DisplayName("getOrderOrThrow 주문 없으면 예외")
        void getOrderOrThrowNotFoundThrowsException() {
            // given
            given(loadOrderPort.findById("non-existent"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrderOrThrow("non-existent"))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    // =====================================================
    // 주문 상태 변경 테스트
    // =====================================================

    @Nested
    @DisplayName("주문 상태 변경")
    class UpdateStatus {

        @Test
        @DisplayName("결제 완료 성공")
        void payOrderSuccess() {
            // given
            Order order = createOrder();

            given(loadOrderPort.findById(order.getId()))
                    .willReturn(Optional.of(order));
            given(saveOrderPort.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Order result = orderService.payOrder(order.getId());

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID);
            verify(orderSearchPort).index(any(Order.class));
            verify(orderEventPort).publishAsync(any(OrderStatusChangedEvent.class));
        }

        @Test
        @DisplayName("주문 확인 성공")
        void confirmOrderSuccess() {
            // given
            Order order = createPaidOrder();

            given(loadOrderPort.findById(order.getId()))
                    .willReturn(Optional.of(order));
            given(saveOrderPort.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Order result = orderService.confirmOrder(order.getId());

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("준비 시작 성공")
        void startPreparingSuccess() {
            // given
            Order order = createConfirmedOrder();

            given(loadOrderPort.findById(order.getId()))
                    .willReturn(Optional.of(order));
            given(saveOrderPort.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Order result = orderService.startPreparing(order.getId());

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("배송 시작 성공")
        void shipOrderSuccess() {
            // given
            Order order = createPreparingOrder();

            given(loadOrderPort.findById(order.getId()))
                    .willReturn(Optional.of(order));
            given(saveOrderPort.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Order result = orderService.shipOrder(order.getId());

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        }

        @Test
        @DisplayName("주문 취소 성공")
        void cancelOrderSuccess() {
            // given
            Order order = createOrder();

            given(loadOrderPort.findById(order.getId()))
                    .willReturn(Optional.of(order));
            given(saveOrderPort.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Order result = orderService.cancelOrder(order.getId());

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("반품 요청 성공")
        void requestReturnSuccess() {
            // given
            Order order = createDeliveredOrder();

            given(loadOrderPort.findById(order.getId()))
                    .willReturn(Optional.of(order));
            given(saveOrderPort.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Order result = orderService.requestReturn(order.getId());

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);
        }

        @Test
        @DisplayName("updateStatus로 결제 상태 변경")
        void updateStatusToPaid() {
            // given
            Order order = createOrder();

            given(loadOrderPort.findById(order.getId()))
                    .willReturn(Optional.of(order));
            given(saveOrderPort.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Order result = orderService.updateStatus(order.getId(), OrderStatus.PAID);

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("지원하지 않는 상태로 변경 시 예외")
        void updateStatusToUnsupportedThrowsException() {
            // when & then
            assertThatThrownBy(() ->
                    orderService.updateStatus(ORDER_ID, OrderStatus.DELIVERED))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot directly update to status");
        }

        @Test
        @DisplayName("존재하지 않는 주문 상태 변경 시 예외")
        void updateStatusNotFoundThrowsException() {
            // given
            given(loadOrderPort.findById("non-existent"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.payOrder("non-existent"))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    // =====================================================
    // 주문 검색 테스트
    // =====================================================

    @Nested
    @DisplayName("주문 검색")
    class SearchOrders {

        @Test
        @DisplayName("고객별 주문 조회")
        void findByCustomerIdSuccess() {
            // given
            List<Order> orders = List.of(createOrder());

            given(orderSearchPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(orders);

            // when
            List<Order> result = orderService.findByCustomerId(CUSTOMER_ID);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCustomerId()).isEqualTo(CUSTOMER_ID);
        }

        @Test
        @DisplayName("판매자별 주문 조회")
        void findBySellerIdSuccess() {
            // given
            List<Order> orders = List.of(createOrder());

            given(orderSearchPort.findBySellerId(SELLER_ID))
                    .willReturn(orders);

            // when
            List<Order> result = orderService.findBySellerId(SELLER_ID);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSellerId()).isEqualTo(SELLER_ID);
        }

        @Test
        @DisplayName("검색 쿼리로 주문 검색")
        void searchOrdersSuccess() {
            // given
            List<Order> orders = List.of(createOrder());
            SearchOrderQuery query = SearchOrderQuery.builder()
                    .customerId(CUSTOMER_ID)
                    .build();

            given(orderSearchPort.search(query))
                    .willReturn(orders);

            // when
            List<Order> result = orderService.searchOrders(query);

            // then
            assertThat(result).hasSize(1);
        }
    }
}

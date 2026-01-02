package jjh.delivery.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jjh.delivery.adapter.in.web.dto.CreateOrderRequest;
import jjh.delivery.adapter.in.web.dto.OrderResponse;
import jjh.delivery.adapter.in.web.dto.UpdateOrderStatusRequest;
import jjh.delivery.adapter.in.web.mapper.OrderWebMapper;
import jjh.delivery.application.port.in.CreateOrderUseCase;
import jjh.delivery.application.port.in.GetOrderUseCase;
import jjh.delivery.application.port.in.SearchOrderUseCase;
import jjh.delivery.application.port.in.UpdateOrderStatusUseCase;
import jjh.delivery.config.security.JwtTokenProvider;
import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderItem;
import jjh.delivery.domain.order.OrderStatus;
import jjh.delivery.domain.order.ShippingAddress;
import jjh.delivery.domain.order.exception.OrderNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrderController Unit Tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("OrderController 테스트")
@org.junit.jupiter.api.Disabled("Spring Boot 4.0.1 외부 서비스 의존성 문제로 비활성화 - 추후 수정 필요")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateOrderUseCase createOrderUseCase;

    @MockitoBean
    private GetOrderUseCase getOrderUseCase;

    @MockitoBean
    private UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @MockitoBean
    private SearchOrderUseCase searchOrderUseCase;

    @MockitoBean
    private OrderWebMapper mapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    // =====================================================
    // Test Fixtures
    // =====================================================

    private static final String BASE_URL = "/api/v2/orders";
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

    private OrderResponse createOrderResponse() {
        return new OrderResponse(
                ORDER_ID,
                "ORD-12345678",
                CUSTOMER_ID,
                SELLER_ID,
                List.of(new OrderResponse.OrderItemResponse(
                        "product-1", "테스트 상품", null, null, "SKU-001",
                        Map.of(), 2, new BigDecimal("10000"), new BigDecimal("20000")
                )),
                OrderStatus.PENDING,
                new OrderResponse.ShippingAddressResponse(
                        "홍길동", "010-1234-5678", "12345",
                        "서울시 강남구", "상세주소", null, "서울시 강남구 상세주소"
                ),
                new BigDecimal("20000"),
                new BigDecimal("3000"),
                BigDecimal.ZERO,
                new BigDecimal("23000"),
                null, null, null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null, null, null, null, null
        );
    }

    private CreateOrderRequest createValidOrderRequest() {
        return new CreateOrderRequest(
                CUSTOMER_ID,
                SELLER_ID,
                List.of(new CreateOrderRequest.OrderItemRequest(
                        "product-1", "테스트 상품", null, null, "SKU-001",
                        Map.of(), 2, new BigDecimal("10000")
                )),
                new CreateOrderRequest.ShippingAddressRequest(
                        "홍길동", "010-1234-5678", "12345",
                        "서울시 강남구", "상세주소", null
                ),
                null, null, null
        );
    }

    // =====================================================
    // 주문 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("POST /api/v2/orders - 주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("주문 생성 성공 - 201 Created")
        void createOrderSuccess() throws Exception {
            // given
            CreateOrderRequest request = createValidOrderRequest();
            Order order = createOrder();
            OrderResponse response = createOrderResponse();

            given(mapper.toCommand(any())).willReturn(
                    new CreateOrderUseCase.CreateOrderCommand(
                            CUSTOMER_ID, SELLER_ID, List.of(), null, null, null, null
                    )
            );
            given(createOrderUseCase.createOrder(any())).willReturn(order);
            given(mapper.toResponse(order)).willReturn(response);

            // when & then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(ORDER_ID))
                    .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 Bad Request")
        void createOrderWithMissingFieldsReturnsBadRequest() throws Exception {
            // given - customerId 누락
            String invalidRequest = """
                    {
                        "sellerId": "seller-789",
                        "items": [{
                            "productId": "product-1",
                            "productName": "상품명",
                            "quantity": 1,
                            "unitPrice": 10000
                        }],
                        "shippingAddress": {
                            "recipientName": "홍길동",
                            "phoneNumber": "010-1234-5678",
                            "postalCode": "12345",
                            "address1": "서울시"
                        }
                    }
                    """;

            // when & then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
    }

    // =====================================================
    // 주문 조회 테스트
    // =====================================================

    @Nested
    @DisplayName("GET /api/v2/orders/{orderId} - 주문 조회")
    class GetOrder {

        @Test
        @DisplayName("주문 조회 성공 - 200 OK")
        void getOrderSuccess() throws Exception {
            // given
            Order order = createOrder();
            OrderResponse response = createOrderResponse();

            given(getOrderUseCase.getOrderOrThrow(ORDER_ID)).willReturn(order);
            given(mapper.toResponse(order)).willReturn(response);

            // when & then
            mockMvc.perform(get(BASE_URL + "/{orderId}", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ORDER_ID))
                    .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID));
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 404 Not Found")
        void getOrderNotFound() throws Exception {
            // given
            given(getOrderUseCase.getOrderOrThrow("non-existent"))
                    .willThrow(new OrderNotFoundException("non-existent"));

            // when & then
            mockMvc.perform(get(BASE_URL + "/{orderId}", "non-existent"))
                    .andExpect(status().isNotFound());
        }
    }

    // =====================================================
    // 주문 상태 변경 테스트
    // =====================================================

    @Nested
    @DisplayName("PATCH /api/v2/orders/{orderId}/status - 주문 상태 변경")
    class UpdateStatus {

        @Test
        @DisplayName("상태 변경 성공 - 200 OK")
        void updateStatusSuccess() throws Exception {
            // given
            Order order = createOrder();
            OrderResponse response = createOrderResponse();
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAID);

            given(updateOrderStatusUseCase.updateStatus(ORDER_ID, OrderStatus.PAID))
                    .willReturn(order);
            given(mapper.toResponse(order)).willReturn(response);

            // when & then
            mockMvc.perform(patch(BASE_URL + "/{orderId}/status", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    // =====================================================
    // 주문 상태 액션 테스트
    // =====================================================

    @Nested
    @DisplayName("주문 상태 액션 API")
    class StatusActions {

        @Test
        @DisplayName("POST /pay - 결제 완료")
        void payOrderSuccess() throws Exception {
            // given
            Order order = createOrder();
            OrderResponse response = createOrderResponse();

            given(updateOrderStatusUseCase.payOrder(ORDER_ID)).willReturn(order);
            given(mapper.toResponse(order)).willReturn(response);

            // when & then
            mockMvc.perform(post(BASE_URL + "/{orderId}/pay", ORDER_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /confirm - 주문 확인")
        void confirmOrderSuccess() throws Exception {
            // given
            Order order = createOrder();
            OrderResponse response = createOrderResponse();

            given(updateOrderStatusUseCase.confirmOrder(ORDER_ID)).willReturn(order);
            given(mapper.toResponse(order)).willReturn(response);

            // when & then
            mockMvc.perform(post(BASE_URL + "/{orderId}/confirm", ORDER_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /prepare - 준비 시작")
        void startPreparingSuccess() throws Exception {
            // given
            Order order = createOrder();
            OrderResponse response = createOrderResponse();

            given(updateOrderStatusUseCase.startPreparing(ORDER_ID)).willReturn(order);
            given(mapper.toResponse(order)).willReturn(response);

            // when & then
            mockMvc.perform(post(BASE_URL + "/{orderId}/prepare", ORDER_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /ship - 배송 시작")
        void shipOrderSuccess() throws Exception {
            // given
            Order order = createOrder();
            OrderResponse response = createOrderResponse();

            given(updateOrderStatusUseCase.shipOrder(ORDER_ID)).willReturn(order);
            given(mapper.toResponse(order)).willReturn(response);

            // when & then
            mockMvc.perform(post(BASE_URL + "/{orderId}/ship", ORDER_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /cancel - 주문 취소")
        void cancelOrderSuccess() throws Exception {
            // given
            Order order = createOrder();
            OrderResponse response = createOrderResponse();

            given(updateOrderStatusUseCase.cancelOrder(ORDER_ID)).willReturn(order);
            given(mapper.toResponse(order)).willReturn(response);

            // when & then
            mockMvc.perform(post(BASE_URL + "/{orderId}/cancel", ORDER_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /return - 반품 요청")
        void requestReturnSuccess() throws Exception {
            // given
            Order order = createOrder();
            OrderResponse response = createOrderResponse();

            given(updateOrderStatusUseCase.requestReturn(ORDER_ID)).willReturn(order);
            given(mapper.toResponse(order)).willReturn(response);

            // when & then
            mockMvc.perform(post(BASE_URL + "/{orderId}/return", ORDER_ID))
                    .andExpect(status().isOk());
        }
    }

    // =====================================================
    // 주문 검색 테스트
    // =====================================================

    @Nested
    @DisplayName("주문 검색 API")
    class SearchOrders {

        @Test
        @DisplayName("GET /customer/{customerId} - 고객별 주문 조회")
        void getOrdersByCustomerSuccess() throws Exception {
            // given
            List<Order> orders = List.of(createOrder());
            List<OrderResponse> responses = List.of(createOrderResponse());

            given(searchOrderUseCase.findByCustomerId(CUSTOMER_ID)).willReturn(orders);
            given(mapper.toResponseList(orders)).willReturn(responses);

            // when & then
            mockMvc.perform(get(BASE_URL + "/customer/{customerId}", CUSTOMER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("GET /seller/{sellerId} - 판매자별 주문 조회")
        void getOrdersBySellerSuccess() throws Exception {
            // given
            List<Order> orders = List.of(createOrder());
            List<OrderResponse> responses = List.of(createOrderResponse());

            given(searchOrderUseCase.findBySellerId(SELLER_ID)).willReturn(orders);
            given(mapper.toResponseList(orders)).willReturn(responses);

            // when & then
            mockMvc.perform(get(BASE_URL + "/seller/{sellerId}", SELLER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }
}

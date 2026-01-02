package jjh.delivery.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Order Aggregate Root Unit Tests
 */
@DisplayName("Order 도메인 테스트")
class OrderTest {

    // =====================================================
    // Test Fixtures
    // =====================================================

    private ShippingAddress createShippingAddress() {
        return ShippingAddress.of(
                "홍길동",
                "010-1234-5678",
                "12345",
                "서울시 강남구 테헤란로 123",
                "456호",
                "부재시 경비실"
        );
    }

    private OrderItem createOrderItem(String productId, String productName, int quantity, BigDecimal unitPrice) {
        return OrderItem.of(productId, productName, quantity, unitPrice);
    }

    private Order.Builder createValidOrderBuilder() {
        return Order.builder()
                .customerId("customer-123")
                .sellerId("seller-456")
                .shippingAddress(createShippingAddress())
                .addItem(createOrderItem("product-1", "테스트 상품", 2, new BigDecimal("10000")));
    }

    // =====================================================
    // 주문 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("주문 생성")
    class OrderCreation {

        @Test
        @DisplayName("필수 필드로 주문 생성 성공")
        void createOrderWithRequiredFields() {
            // given & when
            Order order = createValidOrderBuilder().build();

            // then
            assertThat(order.getId()).isNotNull();
            assertThat(order.getOrderNumber()).startsWith("ORD-");
            assertThat(order.getCustomerId()).isEqualTo("customer-123");
            assertThat(order.getSellerId()).isEqualTo("seller-456");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("customerId 없이 주문 생성 시 예외 발생")
        void createOrderWithoutCustomerIdThrowsException() {
            assertThatThrownBy(() ->
                    Order.builder()
                            .sellerId("seller-456")
                            .shippingAddress(createShippingAddress())
                            .addItem(createOrderItem("product-1", "상품", 1, new BigDecimal("10000")))
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("customerId");
        }

        @Test
        @DisplayName("sellerId 없이 주문 생성 시 예외 발생")
        void createOrderWithoutSellerIdThrowsException() {
            assertThatThrownBy(() ->
                    Order.builder()
                            .customerId("customer-123")
                            .shippingAddress(createShippingAddress())
                            .addItem(createOrderItem("product-1", "상품", 1, new BigDecimal("10000")))
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sellerId");
        }

        @Test
        @DisplayName("shippingAddress 없이 주문 생성 시 예외 발생")
        void createOrderWithoutShippingAddressThrowsException() {
            assertThatThrownBy(() ->
                    Order.builder()
                            .customerId("customer-123")
                            .sellerId("seller-456")
                            .addItem(createOrderItem("product-1", "상품", 1, new BigDecimal("10000")))
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("shippingAddress");
        }

        @Test
        @DisplayName("주문 항목 없이 주문 생성 시 예외 발생")
        void createOrderWithoutItemsThrowsException() {
            assertThatThrownBy(() ->
                    Order.builder()
                            .customerId("customer-123")
                            .sellerId("seller-456")
                            .shippingAddress(createShippingAddress())
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("item");
        }

        @Test
        @DisplayName("여러 주문 항목으로 주문 생성")
        void createOrderWithMultipleItems() {
            // given
            List<OrderItem> items = List.of(
                    createOrderItem("product-1", "상품1", 2, new BigDecimal("10000")),
                    createOrderItem("product-2", "상품2", 3, new BigDecimal("20000"))
            );

            // when
            Order order = Order.builder()
                    .customerId("customer-123")
                    .sellerId("seller-456")
                    .shippingAddress(createShippingAddress())
                    .items(items)
                    .build();

            // then
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getItemCount()).isEqualTo(2);
            assertThat(order.getTotalQuantity()).isEqualTo(5);
        }
    }

    // =====================================================
    // 금액 계산 테스트
    // =====================================================

    @Nested
    @DisplayName("금액 계산")
    class AmountCalculation {

        @Test
        @DisplayName("소계 금액 자동 계산")
        void calculateSubtotalAmount() {
            // given
            List<OrderItem> items = List.of(
                    createOrderItem("product-1", "상품1", 2, new BigDecimal("10000")),  // 20,000
                    createOrderItem("product-2", "상품2", 3, new BigDecimal("5000"))    // 15,000
            );

            // when
            Order order = Order.builder()
                    .customerId("customer-123")
                    .sellerId("seller-456")
                    .shippingAddress(createShippingAddress())
                    .items(items)
                    .build();

            // then
            assertThat(order.getSubtotalAmount()).isEqualByComparingTo(new BigDecimal("35000"));
        }

        @Test
        @DisplayName("배송비와 할인 포함한 총액 계산")
        void calculateTotalAmountWithShippingAndDiscount() {
            // given & when
            Order order = Order.builder()
                    .customerId("customer-123")
                    .sellerId("seller-456")
                    .shippingAddress(createShippingAddress())
                    .addItem(createOrderItem("product-1", "상품", 2, new BigDecimal("10000")))
                    .shippingFee(new BigDecimal("3000"))
                    .discountAmount(new BigDecimal("2000"))
                    .build();

            // then (20,000 + 3,000 - 2,000 = 21,000)
            assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("21000"));
        }

        @Test
        @DisplayName("배송비와 할인 없이 총액 계산")
        void calculateTotalAmountWithoutShippingAndDiscount() {
            // given & when
            Order order = createValidOrderBuilder().build();

            // then (소계 = 총액)
            assertThat(order.getTotalAmount()).isEqualByComparingTo(order.getSubtotalAmount());
        }
    }

    // =====================================================
    // 상태 전이 테스트
    // =====================================================

    @Nested
    @DisplayName("주문 상태 전이")
    class OrderStatusTransition {

        @Test
        @DisplayName("PENDING → PAID 결제 완료")
        void payOrder() {
            // given
            Order order = createValidOrderBuilder().build();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

            // when
            order.pay();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("PAID → CONFIRMED 주문 확정")
        void confirmOrder() {
            // given
            Order order = createValidOrderBuilder().build();
            order.pay();

            // when
            order.confirm();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(order.getConfirmedAt()).isNotNull();
        }

        @Test
        @DisplayName("전체 배송 프로세스 상태 전이")
        void fullDeliveryProcess() {
            // given
            Order order = createValidOrderBuilder().build();

            // when & then - 전체 플로우
            order.pay();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

            order.confirm();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            order.startPreparing();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);

            order.ship();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(order.getShippedAt()).isNotNull();

            order.inTransit();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_TRANSIT);

            order.outForDelivery();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);

            order.deliver();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
            assertThat(order.getDeliveredAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING에서 CONFIRMED로 직접 전이 시 예외 발생")
        void cannotTransitionFromPendingToConfirmed() {
            // given
            Order order = createValidOrderBuilder().build();

            // when & then
            assertThatThrownBy(order::confirm)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot transition");
        }

        @Test
        @DisplayName("DELIVERED에서 PAID로 전이 시 예외 발생")
        void cannotTransitionFromDeliveredToPaid() {
            // given
            Order order = createValidOrderBuilder()
                    .status(OrderStatus.DELIVERED)
                    .build();

            // when & then
            assertThatThrownBy(order::pay)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot transition");
        }
    }

    // =====================================================
    // 주문 취소 테스트
    // =====================================================

    @Nested
    @DisplayName("주문 취소")
    class OrderCancellation {

        @Test
        @DisplayName("PENDING 상태에서 취소 가능")
        void cancelFromPending() {
            // given
            Order order = createValidOrderBuilder().build();

            // when
            order.cancel();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("PAID 상태에서 취소 가능")
        void cancelFromPaid() {
            // given
            Order order = createValidOrderBuilder().build();
            order.pay();

            // when
            order.cancel();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("SHIPPED 상태에서 취소 가능")
        void cancelFromShipped() {
            // given
            Order order = createValidOrderBuilder().build();
            order.pay();
            order.confirm();
            order.startPreparing();
            order.ship();

            // when
            order.cancel();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("IN_TRANSIT 상태에서 취소 불가")
        void cannotCancelFromInTransit() {
            // given
            Order order = createValidOrderBuilder().build();
            order.pay();
            order.confirm();
            order.startPreparing();
            order.ship();
            order.inTransit();

            // when & then
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot cancel");
        }

        @Test
        @DisplayName("DELIVERED 상태에서 취소 불가")
        void cannotCancelFromDelivered() {
            // given
            Order order = createValidOrderBuilder()
                    .status(OrderStatus.DELIVERED)
                    .build();

            // when & then
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot cancel");
        }
    }

    // =====================================================
    // 반품 테스트
    // =====================================================

    @Nested
    @DisplayName("반품 처리")
    class ReturnHandling {

        @Test
        @DisplayName("DELIVERED 상태에서 반품 요청 가능")
        void requestReturnFromDelivered() {
            // given
            Order order = createValidOrderBuilder()
                    .status(OrderStatus.DELIVERED)
                    .build();

            // when
            order.requestReturn();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);
        }

        @Test
        @DisplayName("PENDING 상태에서 반품 요청 불가")
        void cannotRequestReturnFromPending() {
            // given
            Order order = createValidOrderBuilder().build();

            // when & then
            assertThatThrownBy(order::requestReturn)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot request return");
        }

        @Test
        @DisplayName("반품 완료 처리")
        void completeReturn() {
            // given
            Order order = createValidOrderBuilder()
                    .status(OrderStatus.DELIVERED)
                    .build();
            order.requestReturn();

            // when
            order.completeReturn();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.RETURNED);
        }

        @Test
        @DisplayName("반품 거절 시 DELIVERED 상태로 복원")
        void rejectReturnRestoresToDelivered() {
            // given
            Order order = createValidOrderBuilder()
                    .status(OrderStatus.DELIVERED)
                    .build();
            order.requestReturn();

            // when
            order.rejectReturn();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("RETURN_REQUESTED 외 상태에서 반품 거절 시 예외")
        void cannotRejectReturnFromOtherStatus() {
            // given
            Order order = createValidOrderBuilder()
                    .status(OrderStatus.DELIVERED)
                    .build();

            // when & then
            assertThatThrownBy(order::rejectReturn)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("RETURN_REQUESTED");
        }
    }

    // =====================================================
    // 조회 메서드 테스트
    // =====================================================

    @Nested
    @DisplayName("조회 메서드")
    class QueryMethods {

        @Test
        @DisplayName("주문 항목 수 조회")
        void getItemCount() {
            // given
            Order order = Order.builder()
                    .customerId("customer-123")
                    .sellerId("seller-456")
                    .shippingAddress(createShippingAddress())
                    .addItem(createOrderItem("product-1", "상품1", 2, new BigDecimal("10000")))
                    .addItem(createOrderItem("product-2", "상품2", 3, new BigDecimal("5000")))
                    .build();

            // then
            assertThat(order.getItemCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("총 상품 수량 조회")
        void getTotalQuantity() {
            // given
            Order order = Order.builder()
                    .customerId("customer-123")
                    .sellerId("seller-456")
                    .shippingAddress(createShippingAddress())
                    .addItem(createOrderItem("product-1", "상품1", 2, new BigDecimal("10000")))
                    .addItem(createOrderItem("product-2", "상품2", 3, new BigDecimal("5000")))
                    .build();

            // then
            assertThat(order.getTotalQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("특정 상품 포함 여부 확인")
        void containsProduct() {
            // given
            Order order = createValidOrderBuilder().build();

            // then
            assertThat(order.containsProduct("product-1")).isTrue();
            assertThat(order.containsProduct("product-999")).isFalse();
        }

        @Test
        @DisplayName("주문 항목 목록은 불변")
        void itemsListIsImmutable() {
            // given
            Order order = createValidOrderBuilder().build();
            List<OrderItem> items = order.getItems();

            // when & then
            assertThatThrownBy(() -> items.add(createOrderItem("new", "신규", 1, BigDecimal.ONE)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}

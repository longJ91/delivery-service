package jjh.delivery.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * OrderStatus Enum Unit Tests
 */
@DisplayName("OrderStatus 도메인 테스트")
class OrderStatusTest {

    // =====================================================
    // 상태 전이 테스트
    // =====================================================

    @Nested
    @DisplayName("상태 전이 규칙")
    class StatusTransition {

        @Test
        @DisplayName("PENDING에서 PAID로 전이 가능")
        void canTransitionFromPendingToPaid() {
            assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PAID)).isTrue();
        }

        @Test
        @DisplayName("PENDING에서 CANCELLED로 전이 가능")
        void canTransitionFromPendingToCancelled() {
            assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PENDING에서 CONFIRMED로 직접 전이 불가")
        void cannotTransitionFromPendingToConfirmed() {
            assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED)).isFalse();
        }

        @Test
        @DisplayName("PAID에서 CONFIRMED로 전이 가능")
        void canTransitionFromPaidToConfirmed() {
            assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
        }

        @Test
        @DisplayName("DELIVERED에서 RETURN_REQUESTED로 전이 가능")
        void canTransitionFromDeliveredToReturnRequested() {
            assertThat(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.RETURN_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("RETURN_REQUESTED에서 RETURNED로 전이 가능")
        void canTransitionFromReturnRequestedToReturned() {
            assertThat(OrderStatus.RETURN_REQUESTED.canTransitionTo(OrderStatus.RETURNED)).isTrue();
        }

        @Test
        @DisplayName("RETURN_REQUESTED에서 DELIVERED로 전이 가능 (거절)")
        void canTransitionFromReturnRequestedToDelivered() {
            assertThat(OrderStatus.RETURN_REQUESTED.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
        }

        @Test
        @DisplayName("CANCELLED에서 다른 상태로 전이 불가")
        void cannotTransitionFromCancelled() {
            for (OrderStatus status : OrderStatus.values()) {
                assertThat(OrderStatus.CANCELLED.canTransitionTo(status)).isFalse();
            }
        }

        @Test
        @DisplayName("RETURNED에서 다른 상태로 전이 불가")
        void cannotTransitionFromReturned() {
            for (OrderStatus status : OrderStatus.values()) {
                assertThat(OrderStatus.RETURNED.canTransitionTo(status)).isFalse();
            }
        }
    }

    // =====================================================
    // 취소 가능 여부 테스트
    // =====================================================

    @Nested
    @DisplayName("취소 가능 여부")
    class CancellableCheck {

        @Test
        @DisplayName("PENDING 상태에서 취소 가능")
        void pendingIsCancellable() {
            assertThat(OrderStatus.PENDING.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("PAID 상태에서 취소 가능")
        void paidIsCancellable() {
            assertThat(OrderStatus.PAID.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("CONFIRMED 상태에서 취소 가능")
        void confirmedIsCancellable() {
            assertThat(OrderStatus.CONFIRMED.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("PREPARING 상태에서 취소 가능")
        void preparingIsCancellable() {
            assertThat(OrderStatus.PREPARING.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("SHIPPED 상태에서 취소 가능")
        void shippedIsCancellable() {
            assertThat(OrderStatus.SHIPPED.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("IN_TRANSIT 상태에서 취소 불가")
        void inTransitIsNotCancellable() {
            assertThat(OrderStatus.IN_TRANSIT.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("OUT_FOR_DELIVERY 상태에서 취소 불가")
        void outForDeliveryIsNotCancellable() {
            assertThat(OrderStatus.OUT_FOR_DELIVERY.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("DELIVERED 상태에서 취소 불가")
        void deliveredIsNotCancellable() {
            assertThat(OrderStatus.DELIVERED.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED 상태에서 취소 불가")
        void cancelledIsNotCancellable() {
            assertThat(OrderStatus.CANCELLED.isCancellable()).isFalse();
        }
    }

    // =====================================================
    // 반품 가능 여부 테스트
    // =====================================================

    @Nested
    @DisplayName("반품 가능 여부")
    class ReturnableCheck {

        @Test
        @DisplayName("DELIVERED 상태에서만 반품 가능")
        void onlyDeliveredIsReturnable() {
            assertThat(OrderStatus.DELIVERED.isReturnable()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"PENDING", "PAID", "CONFIRMED",
                "PREPARING", "SHIPPED", "IN_TRANSIT", "OUT_FOR_DELIVERY",
                "CANCELLED", "RETURN_REQUESTED", "RETURNED"})
        @DisplayName("DELIVERED 외 상태에서 반품 불가")
        void otherStatusesAreNotReturnable(OrderStatus status) {
            assertThat(status.isReturnable()).isFalse();
        }
    }

    // =====================================================
    // 종료 상태 확인 테스트
    // =====================================================

    @Nested
    @DisplayName("종료 상태 확인")
    class TerminalCheck {

        @Test
        @DisplayName("CANCELLED는 종료 상태")
        void cancelledIsTerminal() {
            assertThat(OrderStatus.CANCELLED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("RETURNED는 종료 상태")
        void returnedIsTerminal() {
            assertThat(OrderStatus.RETURNED.isTerminal()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"PENDING", "PAID", "CONFIRMED",
                "PREPARING", "SHIPPED", "IN_TRANSIT", "OUT_FOR_DELIVERY",
                "DELIVERED", "RETURN_REQUESTED"})
        @DisplayName("CANCELLED, RETURNED 외 상태는 종료 상태가 아님")
        void otherStatusesAreNotTerminal(OrderStatus status) {
            assertThat(status.isTerminal()).isFalse();
        }
    }

    // =====================================================
    // 배송 진행중 상태 확인 테스트
    // =====================================================

    @Nested
    @DisplayName("배송 진행중 상태 확인")
    class InDeliveryCheck {

        @Test
        @DisplayName("SHIPPED는 배송 진행중")
        void shippedIsInDelivery() {
            assertThat(OrderStatus.SHIPPED.isInDelivery()).isTrue();
        }

        @Test
        @DisplayName("IN_TRANSIT는 배송 진행중")
        void inTransitIsInDelivery() {
            assertThat(OrderStatus.IN_TRANSIT.isInDelivery()).isTrue();
        }

        @Test
        @DisplayName("OUT_FOR_DELIVERY는 배송 진행중")
        void outForDeliveryIsInDelivery() {
            assertThat(OrderStatus.OUT_FOR_DELIVERY.isInDelivery()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"PENDING", "PAID", "CONFIRMED",
                "PREPARING", "DELIVERED", "CANCELLED", "RETURN_REQUESTED", "RETURNED"})
        @DisplayName("SHIPPED, IN_TRANSIT, OUT_FOR_DELIVERY 외 상태는 배송 진행중이 아님")
        void otherStatusesAreNotInDelivery(OrderStatus status) {
            assertThat(status.isInDelivery()).isFalse();
        }
    }

    // =====================================================
    // 전체 상태 플로우 테스트
    // =====================================================

    @Nested
    @DisplayName("전체 상태 플로우")
    class FullFlow {

        @Test
        @DisplayName("정상 배송 완료 플로우")
        void normalDeliveryFlow() {
            // PENDING → PAID → CONFIRMED → PREPARING → SHIPPED → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
            assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PAID)).isTrue();
            assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
            assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PREPARING)).isTrue();
            assertThat(OrderStatus.PREPARING.canTransitionTo(OrderStatus.SHIPPED)).isTrue();
            assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.IN_TRANSIT)).isTrue();
            assertThat(OrderStatus.IN_TRANSIT.canTransitionTo(OrderStatus.OUT_FOR_DELIVERY)).isTrue();
            assertThat(OrderStatus.OUT_FOR_DELIVERY.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
        }

        @Test
        @DisplayName("반품 플로우")
        void returnFlow() {
            // DELIVERED → RETURN_REQUESTED → RETURNED
            assertThat(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.RETURN_REQUESTED)).isTrue();
            assertThat(OrderStatus.RETURN_REQUESTED.canTransitionTo(OrderStatus.RETURNED)).isTrue();
        }
    }
}

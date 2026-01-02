package jjh.delivery.domain.shipment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Shipment Aggregate Root Unit Tests
 */
@DisplayName("Shipment 도메인 테스트")
class ShipmentTest {

    // =====================================================
    // Test Fixtures
    // =====================================================

    private Shipment.Builder createValidShipmentBuilder() {
        return Shipment.builder()
                .orderId("order-123")
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(3));
    }

    // =====================================================
    // 배송 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("배송 생성")
    class ShipmentCreation {

        @Test
        @DisplayName("필수 필드로 배송 생성 성공")
        void createShipmentWithRequiredFields() {
            // given & when
            Shipment shipment = createValidShipmentBuilder().build();

            // then
            assertThat(shipment.getId()).isNotNull();
            assertThat(shipment.getOrderId()).isEqualTo("order-123");
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PENDING);
            assertThat(shipment.getTrackingEvents()).isEmpty();
            assertThat(shipment.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("orderId 없이 생성 시 예외 발생")
        void createWithoutOrderIdThrowsException() {
            assertThatThrownBy(() ->
                    Shipment.builder().build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("orderId");
        }
    }

    // =====================================================
    // 운송장 등록 테스트
    // =====================================================

    @Nested
    @DisplayName("운송장 등록")
    class TrackingRegistration {

        @Test
        @DisplayName("운송장 등록 성공")
        void registerTracking() {
            // given
            Shipment shipment = createValidShipmentBuilder().build();

            // when
            shipment.registerTracking(ShippingCarrier.CJ_LOGISTICS, "123456789012");

            // then
            assertThat(shipment.getCarrier()).isEqualTo(ShippingCarrier.CJ_LOGISTICS);
            assertThat(shipment.getTrackingNumber()).isEqualTo("123456789012");
        }

        @Test
        @DisplayName("PENDING 외 상태에서 운송장 등록 불가")
        void cannotRegisterTrackingAfterPickUp() {
            // given
            Shipment shipment = createValidShipmentBuilder()
                    .status(ShipmentStatus.PICKED_UP)
                    .build();

            // when & then
            assertThatThrownBy(() ->
                    shipment.registerTracking(ShippingCarrier.CJ_LOGISTICS, "123456789012")
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING");
        }
    }

    // =====================================================
    // 배송 상태 전이 테스트
    // =====================================================

    @Nested
    @DisplayName("배송 상태 전이")
    class StatusTransition {

        @Test
        @DisplayName("PENDING → PICKED_UP 집화")
        void pickUp() {
            // given
            Shipment shipment = createValidShipmentBuilder().build();

            // when
            shipment.pickUp("서울 물류센터");

            // then
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PICKED_UP);
            assertThat(shipment.getShippedAt()).isNotNull();
            assertThat(shipment.getTrackingEvents()).hasSize(1);
        }

        @Test
        @DisplayName("전체 배송 플로우")
        void fullDeliveryFlow() {
            // given
            Shipment shipment = createValidShipmentBuilder().build();

            // when & then
            shipment.pickUp("서울 물류센터");
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PICKED_UP);

            shipment.inTransit("대전 허브", "대전 허브로 이동 중");
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);

            shipment.outForDelivery("강남 영업소");
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.OUT_FOR_DELIVERY);

            shipment.deliver("고객 주소");
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
            assertThat(shipment.getDeliveredAt()).isNotNull();
            assertThat(shipment.isDelivered()).isTrue();
        }

        @Test
        @DisplayName("잘못된 상태 전이 시 예외")
        void invalidTransitionThrowsException() {
            // given
            Shipment shipment = createValidShipmentBuilder().build();

            // when & then - PENDING에서 바로 DELIVERED로 전이 불가
            assertThatThrownBy(() -> shipment.deliver("위치"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot transition");
        }
    }

    // =====================================================
    // 배송 실패 및 재시도 테스트
    // =====================================================

    @Nested
    @DisplayName("배송 실패 및 재시도")
    class FailureAndRetry {

        @Test
        @DisplayName("배송 실패")
        void failDeliveryAttempt() {
            // given
            Shipment shipment = createValidShipmentBuilder().build();
            shipment.pickUp("물류센터");
            shipment.inTransit("허브", "이동 중");
            shipment.outForDelivery("배송지");

            // when
            shipment.failDeliveryAttempt("배송지", "부재중");

            // then
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.FAILED_ATTEMPT);
            assertThat(shipment.getLatestTrackingEvent().description()).contains("부재중");
        }

        @Test
        @DisplayName("배송 재시도")
        void retryDelivery() {
            // given
            Shipment shipment = createValidShipmentBuilder()
                    .status(ShipmentStatus.FAILED_ATTEMPT)
                    .build();

            // when
            shipment.retryDelivery("배송지");

            // then
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.OUT_FOR_DELIVERY);
        }

        @Test
        @DisplayName("FAILED_ATTEMPT 외 상태에서 재시도 불가")
        void cannotRetryFromNonFailedStatus() {
            // given
            Shipment shipment = createValidShipmentBuilder()
                    .status(ShipmentStatus.IN_TRANSIT)
                    .build();

            // when & then
            assertThatThrownBy(() -> shipment.retryDelivery("배송지"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("FAILED_ATTEMPT");
        }

        @Test
        @DisplayName("반송 처리")
        void returnToSender() {
            // given
            Shipment shipment = createValidShipmentBuilder()
                    .status(ShipmentStatus.FAILED_ATTEMPT)
                    .build();

            // when
            shipment.returnToSender("물류센터", "수취 거부");

            // then
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.RETURNED);
        }

        @Test
        @DisplayName("FAILED_ATTEMPT 외 상태에서 반송 불가")
        void cannotReturnFromNonFailedStatus() {
            // given
            Shipment shipment = createValidShipmentBuilder()
                    .status(ShipmentStatus.IN_TRANSIT)
                    .build();

            // when & then
            assertThatThrownBy(() -> shipment.returnToSender("물류센터", "이유"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("FAILED_ATTEMPT");
        }
    }

    // =====================================================
    // 배송 취소 테스트
    // =====================================================

    @Nested
    @DisplayName("배송 취소")
    class Cancellation {

        @Test
        @DisplayName("PENDING 상태에서 취소 가능")
        void cancelFromPending() {
            // given
            Shipment shipment = createValidShipmentBuilder().build();

            // when
            shipment.cancel("고객 요청");

            // then
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
        }

        @Test
        @DisplayName("PICKED_UP 상태에서 취소 가능")
        void cancelFromPickedUp() {
            // given
            Shipment shipment = createValidShipmentBuilder()
                    .status(ShipmentStatus.PICKED_UP)
                    .build();

            // when
            shipment.cancel("고객 요청");

            // then
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
        }

        @Test
        @DisplayName("DELIVERED 상태에서 취소 불가")
        void cannotCancelFromDelivered() {
            // given
            Shipment shipment = createValidShipmentBuilder()
                    .status(ShipmentStatus.DELIVERED)
                    .build();

            // when & then
            assertThatThrownBy(() -> shipment.cancel("취소 요청"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot cancel");
        }
    }

    // =====================================================
    // 조회 메서드 테스트
    // =====================================================

    @Nested
    @DisplayName("조회 메서드")
    class QueryMethods {

        @Test
        @DisplayName("배송중 여부 확인")
        void isInProgress() {
            // given
            Shipment inTransit = createValidShipmentBuilder()
                    .status(ShipmentStatus.IN_TRANSIT)
                    .build();
            Shipment delivered = createValidShipmentBuilder()
                    .status(ShipmentStatus.DELIVERED)
                    .build();

            // then
            assertThat(inTransit.isInProgress()).isTrue();
            assertThat(delivered.isInProgress()).isFalse();
        }

        @Test
        @DisplayName("배송 완료 여부 확인")
        void isDelivered() {
            // given
            Shipment pending = createValidShipmentBuilder().build();
            Shipment delivered = createValidShipmentBuilder()
                    .status(ShipmentStatus.DELIVERED)
                    .build();

            // then
            assertThat(pending.isDelivered()).isFalse();
            assertThat(delivered.isDelivered()).isTrue();
        }

        @Test
        @DisplayName("최신 추적 이벤트 조회")
        void getLatestTrackingEvent() {
            // given
            Shipment shipment = createValidShipmentBuilder().build();
            assertThat(shipment.getLatestTrackingEvent()).isNull();

            // when
            shipment.pickUp("물류센터");
            shipment.inTransit("허브", "이동 중");

            // then
            TrackingEvent latest = shipment.getLatestTrackingEvent();
            assertThat(latest).isNotNull();
            assertThat(latest.status()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("추적 이벤트 목록은 불변")
        void trackingEventsIsImmutable() {
            // given
            Shipment shipment = createValidShipmentBuilder().build();
            shipment.pickUp("물류센터");

            // when & then
            assertThatThrownBy(() -> shipment.getTrackingEvents().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("예상 배송일 설정")
        void setEstimatedDeliveryDate() {
            // given
            Shipment shipment = createValidShipmentBuilder().build();
            LocalDateTime newDate = LocalDateTime.now().plusDays(5);

            // when
            shipment.setEstimatedDeliveryDate(newDate);

            // then
            assertThat(shipment.getEstimatedDeliveryDate()).isEqualTo(newDate);
        }
    }
}

package jjh.delivery.application.service;

import jjh.delivery.application.port.in.ManageShipmentUseCase.*;
import jjh.delivery.application.port.out.LoadShipmentPort;
import jjh.delivery.application.port.out.SaveShipmentPort;
import jjh.delivery.domain.shipment.Shipment;
import jjh.delivery.domain.shipment.ShipmentStatus;
import jjh.delivery.domain.shipment.ShippingCarrier;
import jjh.delivery.domain.shipment.exception.ShipmentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ShipmentService Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShipmentService 테스트")
class ShipmentServiceTest {

    @Mock
    private LoadShipmentPort loadShipmentPort;

    @Mock
    private SaveShipmentPort saveShipmentPort;

    @InjectMocks
    private ShipmentService shipmentService;

    // =====================================================
    // Test Fixtures
    // =====================================================

    private static final UUID ORDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID NON_EXISTENT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    private Shipment createPendingShipment() {
        return Shipment.builder()
                .orderId(ORDER_UUID)
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(3))
                .build();
    }

    private Shipment createPickedUpShipment() {
        Shipment shipment = createPendingShipment();
        shipment.pickUp("물류센터");
        return shipment;
    }

    private Shipment createInTransitShipment() {
        Shipment shipment = createPickedUpShipment();
        shipment.inTransit("대전 허브", "이동 중");
        return shipment;
    }

    private Shipment createOutForDeliveryShipment() {
        Shipment shipment = createInTransitShipment();
        shipment.outForDelivery("강남 영업소");
        return shipment;
    }

    // =====================================================
    // 배송 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("배송 생성")
    class CreateShipment {

        @Test
        @DisplayName("배송 생성 성공")
        void createShipmentSuccess() {
            // given
            CreateShipmentCommand command = new CreateShipmentCommand(
                    ORDER_UUID.toString(),
                    LocalDateTime.now().plusDays(3)
            );

            given(loadShipmentPort.existsByOrderId(ORDER_UUID)).willReturn(false);
            given(saveShipmentPort.save(any(Shipment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Shipment result = shipmentService.createShipment(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(ORDER_UUID);
            assertThat(result.getStatus()).isEqualTo(ShipmentStatus.PENDING);
            verify(saveShipmentPort).save(any(Shipment.class));
        }

        @Test
        @DisplayName("중복 주문에 대한 배송 생성 시 예외")
        void createShipmentDuplicateOrderThrowsException() {
            // given
            CreateShipmentCommand command = new CreateShipmentCommand(
                    ORDER_UUID.toString(),
                    LocalDateTime.now().plusDays(3)
            );

            given(loadShipmentPort.existsByOrderId(ORDER_UUID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> shipmentService.createShipment(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 해당 주문에 대한 배송이 존재합니다");

            verify(saveShipmentPort, never()).save(any());
        }
    }

    // =====================================================
    // 운송장 등록 테스트
    // =====================================================

    @Nested
    @DisplayName("운송장 등록")
    class RegisterTracking {

        @Test
        @DisplayName("운송장 등록 성공")
        void registerTrackingSuccess() {
            // given
            Shipment shipment = createPendingShipment();
            RegisterTrackingCommand command = new RegisterTrackingCommand(
                    shipment.getId().toString(),
                    ShippingCarrier.CJ_LOGISTICS,
                    "123456789012"
            );

            given(loadShipmentPort.findById(shipment.getId()))
                    .willReturn(Optional.of(shipment));
            given(saveShipmentPort.save(any(Shipment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Shipment result = shipmentService.registerTracking(command);

            // then
            assertThat(result.getCarrier()).isEqualTo(ShippingCarrier.CJ_LOGISTICS);
            assertThat(result.getTrackingNumber()).isEqualTo("123456789012");
            verify(saveShipmentPort).save(shipment);
        }

        @Test
        @DisplayName("존재하지 않는 배송에 운송장 등록 시 예외")
        void registerTrackingNotFoundThrowsException() {
            // given
            RegisterTrackingCommand command = new RegisterTrackingCommand(
                    NON_EXISTENT_UUID.toString(),
                    ShippingCarrier.CJ_LOGISTICS,
                    "123456789012"
            );

            given(loadShipmentPort.findById(NON_EXISTENT_UUID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shipmentService.registerTracking(command))
                    .isInstanceOf(ShipmentNotFoundException.class);

            verify(saveShipmentPort, never()).save(any());
        }
    }

    // =====================================================
    // 배송 상태 변경 테스트 (Strategy Map 패턴)
    // =====================================================

    @Nested
    @DisplayName("배송 상태 변경")
    class UpdateStatus {

        @Test
        @DisplayName("PICKED_UP 상태로 변경")
        void updateStatusToPickedUp() {
            // given
            Shipment shipment = createPendingShipment();
            UpdateShipmentStatusCommand command = new UpdateShipmentStatusCommand(
                    shipment.getId().toString(),
                    ShipmentStatus.PICKED_UP,
                    "서울 물류센터",
                    null
            );

            given(loadShipmentPort.findById(shipment.getId()))
                    .willReturn(Optional.of(shipment));
            given(saveShipmentPort.save(any(Shipment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Shipment result = shipmentService.updateStatus(command);

            // then
            assertThat(result.getStatus()).isEqualTo(ShipmentStatus.PICKED_UP);
            assertThat(result.getShippedAt()).isNotNull();
            verify(saveShipmentPort).save(shipment);
        }

        @Test
        @DisplayName("IN_TRANSIT 상태로 변경")
        void updateStatusToInTransit() {
            // given
            Shipment shipment = createPickedUpShipment();
            UpdateShipmentStatusCommand command = new UpdateShipmentStatusCommand(
                    shipment.getId().toString(),
                    ShipmentStatus.IN_TRANSIT,
                    "대전 허브",
                    "대전 허브로 이동 중"
            );

            given(loadShipmentPort.findById(shipment.getId()))
                    .willReturn(Optional.of(shipment));
            given(saveShipmentPort.save(any(Shipment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Shipment result = shipmentService.updateStatus(command);

            // then
            assertThat(result.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("OUT_FOR_DELIVERY 상태로 변경")
        void updateStatusToOutForDelivery() {
            // given
            Shipment shipment = createInTransitShipment();
            UpdateShipmentStatusCommand command = new UpdateShipmentStatusCommand(
                    shipment.getId().toString(),
                    ShipmentStatus.OUT_FOR_DELIVERY,
                    "강남 영업소",
                    null
            );

            given(loadShipmentPort.findById(shipment.getId()))
                    .willReturn(Optional.of(shipment));
            given(saveShipmentPort.save(any(Shipment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Shipment result = shipmentService.updateStatus(command);

            // then
            assertThat(result.getStatus()).isEqualTo(ShipmentStatus.OUT_FOR_DELIVERY);
        }

        @Test
        @DisplayName("DELIVERED 상태로 변경")
        void updateStatusToDelivered() {
            // given
            Shipment shipment = createOutForDeliveryShipment();
            UpdateShipmentStatusCommand command = new UpdateShipmentStatusCommand(
                    shipment.getId().toString(),
                    ShipmentStatus.DELIVERED,
                    "고객 주소",
                    null
            );

            given(loadShipmentPort.findById(shipment.getId()))
                    .willReturn(Optional.of(shipment));
            given(saveShipmentPort.save(any(Shipment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Shipment result = shipmentService.updateStatus(command);

            // then
            assertThat(result.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
            assertThat(result.getDeliveredAt()).isNotNull();
            assertThat(result.isDelivered()).isTrue();
        }

        @Test
        @DisplayName("FAILED_ATTEMPT 상태로 변경")
        void updateStatusToFailedAttempt() {
            // given
            Shipment shipment = createOutForDeliveryShipment();
            UpdateShipmentStatusCommand command = new UpdateShipmentStatusCommand(
                    shipment.getId().toString(),
                    ShipmentStatus.FAILED_ATTEMPT,
                    "배송지",
                    "부재중"
            );

            given(loadShipmentPort.findById(shipment.getId()))
                    .willReturn(Optional.of(shipment));
            given(saveShipmentPort.save(any(Shipment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Shipment result = shipmentService.updateStatus(command);

            // then
            assertThat(result.getStatus()).isEqualTo(ShipmentStatus.FAILED_ATTEMPT);
        }

        @Test
        @DisplayName("CANCELLED 상태로 변경")
        void updateStatusToCancelled() {
            // given
            Shipment shipment = createPendingShipment();
            UpdateShipmentStatusCommand command = new UpdateShipmentStatusCommand(
                    shipment.getId().toString(),
                    ShipmentStatus.CANCELLED,
                    null,
                    "고객 요청"
            );

            given(loadShipmentPort.findById(shipment.getId()))
                    .willReturn(Optional.of(shipment));
            given(saveShipmentPort.save(any(Shipment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Shipment result = shipmentService.updateStatus(command);

            // then
            assertThat(result.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
        }

        @Test
        @DisplayName("존재하지 않는 배송 상태 변경 시 예외")
        void updateStatusNotFoundThrowsException() {
            // given
            UpdateShipmentStatusCommand command = new UpdateShipmentStatusCommand(
                    NON_EXISTENT_UUID.toString(),
                    ShipmentStatus.PICKED_UP,
                    "물류센터",
                    null
            );

            given(loadShipmentPort.findById(NON_EXISTENT_UUID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shipmentService.updateStatus(command))
                    .isInstanceOf(ShipmentNotFoundException.class);
        }

        @Test
        @DisplayName("지원하지 않는 상태 변경 시 예외")
        void updateStatusUnsupportedThrowsException() {
            // given
            Shipment shipment = createPendingShipment();
            UpdateShipmentStatusCommand command = new UpdateShipmentStatusCommand(
                    shipment.getId().toString(),
                    ShipmentStatus.PENDING, // PENDING은 Strategy Map에 없음
                    "위치",
                    null
            );

            given(loadShipmentPort.findById(shipment.getId()))
                    .willReturn(Optional.of(shipment));

            // when & then
            assertThatThrownBy(() -> shipmentService.updateStatus(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원하지 않는 상태 변경");
        }
    }

    // =====================================================
    // 조회 메서드 테스트
    // =====================================================

    @Nested
    @DisplayName("배송 조회")
    class QueryMethods {

        @Test
        @DisplayName("ID로 배송 조회 성공")
        void getShipmentSuccess() {
            // given
            Shipment shipment = createPendingShipment();

            given(loadShipmentPort.findById(shipment.getId()))
                    .willReturn(Optional.of(shipment));

            // when
            Shipment result = shipmentService.getShipment(shipment.getId());

            // then
            assertThat(result).isEqualTo(shipment);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외")
        void getShipmentNotFoundThrowsException() {
            // given
            given(loadShipmentPort.findById(NON_EXISTENT_UUID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shipmentService.getShipment(NON_EXISTENT_UUID))
                    .isInstanceOf(ShipmentNotFoundException.class);
        }

        @Test
        @DisplayName("주문 ID로 배송 조회 성공")
        void getShipmentByOrderIdSuccess() {
            // given
            Shipment shipment = createPendingShipment();

            given(loadShipmentPort.findByOrderId(ORDER_UUID))
                    .willReturn(Optional.of(shipment));

            // when
            Shipment result = shipmentService.getShipmentByOrderId(ORDER_UUID);

            // then
            assertThat(result.getOrderId()).isEqualTo(ORDER_UUID);
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회 시 예외")
        void getShipmentByOrderIdNotFoundThrowsException() {
            // given
            given(loadShipmentPort.findByOrderId(NON_EXISTENT_UUID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shipmentService.getShipmentByOrderId(NON_EXISTENT_UUID))
                    .isInstanceOf(ShipmentNotFoundException.class)
                    .hasMessageContaining("주문에 대한 배송을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("운송장 번호로 배송 조회 성공")
        void getShipmentByTrackingNumberSuccess() {
            // given
            Shipment shipment = createPendingShipment();
            shipment.registerTracking(ShippingCarrier.CJ_LOGISTICS, "123456789012");

            given(loadShipmentPort.findByTrackingNumber("123456789012"))
                    .willReturn(Optional.of(shipment));

            // when
            Shipment result = shipmentService.getShipmentByTrackingNumber("123456789012");

            // then
            assertThat(result.getTrackingNumber()).isEqualTo("123456789012");
        }

        @Test
        @DisplayName("존재하지 않는 운송장 번호로 조회 시 예외")
        void getShipmentByTrackingNumberNotFoundThrowsException() {
            // given
            given(loadShipmentPort.findByTrackingNumber("non-existent"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shipmentService.getShipmentByTrackingNumber("non-existent"))
                    .isInstanceOf(ShipmentNotFoundException.class)
                    .hasMessageContaining("운송장 번호로 배송을 찾을 수 없습니다");
        }
    }
}

package jjh.delivery.application.service;

import jjh.delivery.application.port.in.ManageSellerUseCase.*;
import jjh.delivery.application.port.out.LoadSellerPort;
import jjh.delivery.application.port.out.SaveSellerPort;
import jjh.delivery.domain.seller.Seller;
import jjh.delivery.domain.seller.SellerStatus;
import jjh.delivery.domain.seller.SellerType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * SellerService Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerService 테스트")
class SellerServiceTest {

    @Mock
    private LoadSellerPort loadSellerPort;

    @Mock
    private SaveSellerPort saveSellerPort;

    @InjectMocks
    private SellerService sellerService;

    // =====================================================
    // Test Fixtures
    // =====================================================

    private static final UUID NON_EXISTENT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final UUID CATEGORY_1_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CATEGORY_2_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private Seller createPendingSeller() {
        return Seller.builder()
                .businessName("테스트 상점")
                .businessNumber("123-45-67890")
                .representativeName("홍길동")
                .email("seller@example.com")
                .phoneNumber("010-1234-5678")
                .sellerType(SellerType.INDIVIDUAL)
                .status(SellerStatus.PENDING)
                .build();
    }

    private Seller createActiveSeller() {
        return Seller.builder()
                .businessName("테스트 상점")
                .businessNumber("123-45-67890")
                .representativeName("홍길동")
                .email("seller@example.com")
                .phoneNumber("010-1234-5678")
                .sellerType(SellerType.INDIVIDUAL)
                .status(SellerStatus.ACTIVE)
                .build();
    }

    private RegisterSellerCommand createRegisterCommand() {
        return new RegisterSellerCommand(
                "새 상점",
                "999-88-77777",
                "김철수",
                "newstore@example.com",
                "010-9999-8888",
                SellerType.CORPORATION,
                null,
                null
        );
    }

    // =====================================================
    // 판매자 등록 테스트
    // =====================================================

    @Nested
    @DisplayName("판매자 등록")
    class RegisterSeller {

        @Test
        @DisplayName("판매자 등록 성공")
        void registerSellerSuccess() {
            // given
            RegisterSellerCommand command = createRegisterCommand();

            given(loadSellerPort.existsByBusinessNumber(command.businessNumber()))
                    .willReturn(false);
            given(loadSellerPort.existsByEmail(command.email()))
                    .willReturn(false);
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.registerSeller(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getBusinessName()).isEqualTo("새 상점");
            assertThat(result.getBusinessNumber()).isEqualTo("999-88-77777");
            assertThat(result.getStatus()).isEqualTo(SellerStatus.PENDING);
            verify(saveSellerPort).save(any(Seller.class));
        }

        @Test
        @DisplayName("중복 사업자번호로 등록 시 예외")
        void registerWithDuplicateBusinessNumberThrowsException() {
            // given
            RegisterSellerCommand command = createRegisterCommand();

            given(loadSellerPort.existsByBusinessNumber(command.businessNumber()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerService.registerSeller(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Business number already registered");

            verify(saveSellerPort, never()).save(any());
        }

        @Test
        @DisplayName("중복 이메일로 등록 시 예외")
        void registerWithDuplicateEmailThrowsException() {
            // given
            RegisterSellerCommand command = createRegisterCommand();

            given(loadSellerPort.existsByBusinessNumber(command.businessNumber()))
                    .willReturn(false);
            given(loadSellerPort.existsByEmail(command.email()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerService.registerSeller(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email already registered");

            verify(saveSellerPort, never()).save(any());
        }

        @Test
        @DisplayName("창고 주소와 함께 등록")
        void registerWithWarehouseAddress() {
            // given
            WarehouseAddressCommand warehouseAddress = new WarehouseAddressCommand(
                    "12345",
                    "경기도 화성시 동탄로 100",
                    "물류센터 1동",
                    "담당자",
                    "010-1234-5678"
            );

            RegisterSellerCommand command = new RegisterSellerCommand(
                    "새 상점",
                    "999-88-77777",
                    "김철수",
                    "newstore@example.com",
                    "010-9999-8888",
                    SellerType.CORPORATION,
                    warehouseAddress,
                    List.of(CATEGORY_1_UUID.toString(), CATEGORY_2_UUID.toString())
            );

            given(loadSellerPort.existsByBusinessNumber(command.businessNumber()))
                    .willReturn(false);
            given(loadSellerPort.existsByEmail(command.email()))
                    .willReturn(false);
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.registerSeller(command);

            // then
            assertThat(result.getWarehouseAddress()).isNotNull();
            assertThat(result.getWarehouseAddress().postalCode()).isEqualTo("12345");
            assertThat(result.getCategoryIds()).containsExactly(CATEGORY_1_UUID, CATEGORY_2_UUID);
        }
    }

    // =====================================================
    // 판매자 정보 수정 테스트
    // =====================================================

    @Nested
    @DisplayName("판매자 정보 수정")
    class UpdateSellerInfo {

        @Test
        @DisplayName("판매자 정보 수정 성공")
        void updateSellerInfoSuccess() {
            // given
            Seller seller = createActiveSeller();
            UpdateSellerInfoCommand command = new UpdateSellerInfoCommand(
                    seller.getId().toString(),
                    "수정된 상점명",
                    "이영희",
                    "updated@example.com",
                    "010-5555-6666"
            );

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.updateSellerInfo(command);

            // then
            assertThat(result.getBusinessName()).isEqualTo("수정된 상점명");
            assertThat(result.getRepresentativeName()).isEqualTo("이영희");
            assertThat(result.getEmail()).isEqualTo("updated@example.com");
            assertThat(result.getPhoneNumber()).isEqualTo("010-5555-6666");
        }

        @Test
        @DisplayName("일부 필드만 수정 시 기존 값 유지")
        void updatePartialFieldsKeepsExistingValues() {
            // given
            Seller seller = createActiveSeller();
            UpdateSellerInfoCommand command = new UpdateSellerInfoCommand(
                    seller.getId().toString(),
                    "수정된 상점명",
                    null,  // 기존 값 유지
                    null,  // 기존 값 유지
                    null   // 기존 값 유지
            );

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.updateSellerInfo(command);

            // then
            assertThat(result.getBusinessName()).isEqualTo("수정된 상점명");
            assertThat(result.getRepresentativeName()).isEqualTo("홍길동");
            assertThat(result.getEmail()).isEqualTo("seller@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 판매자 수정 시 예외")
        void updateNonExistentSellerThrowsException() {
            // given
            UpdateSellerInfoCommand command = new UpdateSellerInfoCommand(
                    NON_EXISTENT_UUID.toString(),
                    "상점명",
                    null,
                    null,
                    null
            );

            given(loadSellerPort.findById(NON_EXISTENT_UUID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerService.updateSellerInfo(command))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    // =====================================================
    // 판매자 상태 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("판매자 상태 관리")
    class SellerStatusManagement {

        @Test
        @DisplayName("판매자 승인")
        void approveSellerSuccess() {
            // given
            Seller seller = createPendingSeller();

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.approveSeller(seller.getId());

            // then
            assertThat(result.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(result.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("판매자 거절")
        void rejectSellerSuccess() {
            // given
            Seller seller = createPendingSeller();

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));

            // when
            sellerService.rejectSeller(seller.getId(), "부적합한 사업자");

            // then
            verify(saveSellerPort).delete(seller.getId());
        }

        @Test
        @DisplayName("PENDING이 아닌 상태에서 거절 시 예외")
        void rejectNonPendingSellerThrowsException() {
            // given
            Seller seller = createActiveSeller();

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));

            // when & then
            assertThatThrownBy(() -> sellerService.rejectSeller(seller.getId(), "이유"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only pending sellers");
        }

        @Test
        @DisplayName("판매자 정지")
        void suspendSellerSuccess() {
            // given
            Seller seller = createActiveSeller();

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.suspendSeller(seller.getId(), "정책 위반");

            // then
            assertThat(result.getStatus()).isEqualTo(SellerStatus.SUSPENDED);
        }

        @Test
        @DisplayName("판매자 활성화")
        void activateSellerSuccess() {
            // given
            Seller seller = Seller.builder()
                    .businessName("테스트 상점")
                    .businessNumber("123-45-67890")
                    .representativeName("홍길동")
                    .email("seller@example.com")
                    .sellerType(SellerType.INDIVIDUAL)
                    .status(SellerStatus.SUSPENDED)
                    .build();

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.activateSeller(seller.getId());

            // then
            assertThat(result.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        @DisplayName("판매자 휴면 전환")
        void makeDormantSuccess() {
            // given
            Seller seller = createActiveSeller();

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.makeDormant(seller.getId());

            // then
            assertThat(result.getStatus()).isEqualTo(SellerStatus.DORMANT);
        }

        @Test
        @DisplayName("판매자 폐업")
        void closeSellerSuccess() {
            // given
            Seller seller = createActiveSeller();

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.closeSeller(seller.getId());

            // then
            assertThat(result.getStatus()).isEqualTo(SellerStatus.CLOSED);
        }
    }

    // =====================================================
    // 카테고리 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("카테고리 관리")
    class CategoryManagement {

        @Test
        @DisplayName("카테고리 추가")
        void addCategorySuccess() {
            // given
            Seller seller = createActiveSeller();

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.addCategory(seller.getId(), CATEGORY_1_UUID);

            // then
            assertThat(result.getCategoryIds()).contains(CATEGORY_1_UUID);
        }

        @Test
        @DisplayName("카테고리 제거")
        void removeCategorySuccess() {
            // given
            Seller seller = Seller.builder()
                    .businessName("테스트 상점")
                    .businessNumber("123-45-67890")
                    .representativeName("홍길동")
                    .email("seller@example.com")
                    .sellerType(SellerType.INDIVIDUAL)
                    .categoryIds(List.of(CATEGORY_1_UUID, CATEGORY_2_UUID))
                    .build();

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.removeCategory(seller.getId(), CATEGORY_1_UUID);

            // then
            assertThat(result.getCategoryIds()).containsExactly(CATEGORY_2_UUID);
        }
    }

    // =====================================================
    // 조회 테스트
    // =====================================================

    @Nested
    @DisplayName("판매자 조회")
    class QueryMethods {

        @Test
        @DisplayName("ID로 판매자 조회")
        void getSellerSuccess() {
            // given
            Seller seller = createActiveSeller();

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));

            // when
            Seller result = sellerService.getSeller(seller.getId());

            // then
            assertThat(result).isEqualTo(seller);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외")
        void getSellerNotFoundThrowsException() {
            // given
            given(loadSellerPort.findById(NON_EXISTENT_UUID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerService.getSeller(NON_EXISTENT_UUID))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("Seller not found");
        }

        @Test
        @DisplayName("사업자번호로 판매자 조회")
        void getSellerByBusinessNumberSuccess() {
            // given
            Seller seller = createActiveSeller();

            given(loadSellerPort.findByBusinessNumber("123-45-67890"))
                    .willReturn(Optional.of(seller));

            // when
            Seller result = sellerService.getSellerByBusinessNumber("123-45-67890");

            // then
            assertThat(result.getBusinessNumber()).isEqualTo("123-45-67890");
        }

        @Test
        @DisplayName("전체 판매자 조회")
        void getAllSellersSuccess() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Seller> expectedPage = new PageImpl<>(
                    List.of(createActiveSeller()),
                    pageable,
                    1
            );

            given(loadSellerPort.findAll(pageable))
                    .willReturn(expectedPage);

            // when
            Page<Seller> result = sellerService.getAllSellers(pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("상태별 판매자 조회")
        void getSellersByStatusSuccess() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Seller> expectedPage = new PageImpl<>(
                    List.of(createPendingSeller()),
                    pageable,
                    1
            );

            given(loadSellerPort.findByStatus(SellerStatus.PENDING, pageable))
                    .willReturn(expectedPage);

            // when
            Page<Seller> result = sellerService.getSellersByStatus(SellerStatus.PENDING, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(SellerStatus.PENDING);
        }

        @Test
        @DisplayName("승인 대기 판매자 조회")
        void getPendingSellersSuccess() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Seller> expectedPage = new PageImpl<>(
                    List.of(createPendingSeller()),
                    pageable,
                    1
            );

            given(loadSellerPort.findByStatus(SellerStatus.PENDING, pageable))
                    .willReturn(expectedPage);

            // when
            Page<Seller> result = sellerService.getPendingSellers(pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    // =====================================================
    // 창고 주소 수정 테스트
    // =====================================================

    @Nested
    @DisplayName("창고 주소 수정")
    class WarehouseAddressUpdate {

        @Test
        @DisplayName("창고 주소 수정 성공")
        void updateWarehouseAddressSuccess() {
            // given
            Seller seller = createActiveSeller();
            WarehouseAddressCommand warehouseAddress = new WarehouseAddressCommand(
                    "12345",
                    "경기도 화성시 동탄로 100",
                    "물류센터 1동",
                    "담당자",
                    "010-1234-5678"
            );

            given(loadSellerPort.findById(seller.getId()))
                    .willReturn(Optional.of(seller));
            given(saveSellerPort.save(any(Seller.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Seller result = sellerService.updateWarehouseAddress(seller.getId(), warehouseAddress);

            // then
            assertThat(result.getWarehouseAddress()).isNotNull();
            assertThat(result.getWarehouseAddress().postalCode()).isEqualTo("12345");
            assertThat(result.getWarehouseAddress().address1()).isEqualTo("경기도 화성시 동탄로 100");
        }
    }
}

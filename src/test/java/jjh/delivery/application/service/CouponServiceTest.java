package jjh.delivery.application.service;

import jjh.delivery.application.port.in.ManageCouponUseCase.CreateCouponCommand;
import jjh.delivery.application.port.in.ManageCouponUseCase.CouponValidationResult;
import jjh.delivery.application.port.in.ManageCouponUseCase.UpdateCouponCommand;
import jjh.delivery.application.port.out.LoadCouponPort;
import jjh.delivery.application.port.out.SaveCouponPort;
import jjh.delivery.domain.promotion.Coupon;
import jjh.delivery.domain.promotion.CouponScope;
import jjh.delivery.domain.promotion.DiscountType;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * CouponService Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService 테스트")
class CouponServiceTest {

    @Mock
    private LoadCouponPort loadCouponPort;

    @Mock
    private SaveCouponPort saveCouponPort;

    @InjectMocks
    private CouponService couponService;

    // =====================================================
    // Test Fixtures
    // =====================================================

    private static final UUID COUPON_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID NON_EXISTENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final String COUPON_CODE = "SAVE1000";

    private Coupon createCoupon() {
        return Coupon.builder()
                .id(COUPON_ID)
                .code(COUPON_CODE)
                .name("1000원 할인 쿠폰")
                .description("1000원 할인")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("1000"))
                .minimumOrderAmount(new BigDecimal("10000"))
                .scope(CouponScope.ALL_PRODUCTS)
                .totalQuantity(100)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();
    }

    private Coupon createPercentageCoupon() {
        return Coupon.builder()
                .id(COUPON_ID)
                .code("PERCENT10")
                .name("10% 할인 쿠폰")
                .description("10% 할인")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .minimumOrderAmount(new BigDecimal("10000"))
                .maximumDiscountAmount(new BigDecimal("5000"))
                .scope(CouponScope.ALL_PRODUCTS)
                .totalQuantity(100)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();
    }

    private CreateCouponCommand createCouponCommand() {
        return new CreateCouponCommand(
                "NEWCOUPON",
                "새 쿠폰",
                "설명",
                DiscountType.FIXED_AMOUNT,
                new BigDecimal("2000"),
                new BigDecimal("15000"),
                null,
                CouponScope.ALL_PRODUCTS,
                null,
                50,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );
    }

    // =====================================================
    // 쿠폰 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("쿠폰 생성")
    class CreateCoupon {

        @Test
        @DisplayName("쿠폰 생성 성공")
        void createCouponSuccess() {
            // given
            CreateCouponCommand command = createCouponCommand();

            given(loadCouponPort.findByCode("NEWCOUPON"))
                    .willReturn(Optional.empty());
            given(saveCouponPort.save(any(Coupon.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Coupon result = couponService.createCoupon(command);

            // then
            assertThat(result.getCode()).isEqualTo("NEWCOUPON");
            assertThat(result.getName()).isEqualTo("새 쿠폰");
            assertThat(result.getDiscountType()).isEqualTo(DiscountType.FIXED_AMOUNT);
            assertThat(result.getDiscountValue()).isEqualByComparingTo(new BigDecimal("2000"));
            verify(saveCouponPort).save(any(Coupon.class));
        }

        @Test
        @DisplayName("중복 코드로 쿠폰 생성 시 예외")
        void createCouponDuplicateCodeThrowsException() {
            // given
            CreateCouponCommand command = createCouponCommand();

            given(loadCouponPort.findByCode("NEWCOUPON"))
                    .willReturn(Optional.of(createCoupon()));

            // when & then
            assertThatThrownBy(() -> couponService.createCoupon(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }
    }

    // =====================================================
    // 쿠폰 수정 테스트
    // =====================================================

    @Nested
    @DisplayName("쿠폰 수정")
    class UpdateCoupon {

        @Test
        @DisplayName("쿠폰 수정 성공")
        void updateCouponSuccess() {
            // given
            Coupon coupon = createCoupon();
            UpdateCouponCommand command = new UpdateCouponCommand(
                    COUPON_ID.toString(), "수정된 이름", "수정된 설명",
                    new BigDecimal("20000"), null, 200, null, null
            );

            given(loadCouponPort.findById(COUPON_ID))
                    .willReturn(Optional.of(coupon));
            given(saveCouponPort.save(any(Coupon.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Coupon result = couponService.updateCoupon(command);

            // then
            assertThat(result.getName()).isEqualTo("수정된 이름");
            assertThat(result.getDescription()).isEqualTo("수정된 설명");
            assertThat(result.getMinimumOrderAmount()).isEqualByComparingTo(new BigDecimal("20000"));
            assertThat(result.getTotalQuantity()).isEqualTo(200);
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 수정 시 예외")
        void updateCouponNotFoundThrowsException() {
            // given
            UpdateCouponCommand command = new UpdateCouponCommand(
                    NON_EXISTENT_ID.toString(), "이름", null, null, null, 0, null, null
            );

            given(loadCouponPort.findById(NON_EXISTENT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.updateCoupon(command))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    // =====================================================
    // 쿠폰 삭제 테스트
    // =====================================================

    @Nested
    @DisplayName("쿠폰 삭제")
    class DeleteCoupon {

        @Test
        @DisplayName("쿠폰 삭제 성공")
        void deleteCouponSuccess() {
            // given
            given(loadCouponPort.findById(COUPON_ID))
                    .willReturn(Optional.of(createCoupon()));

            // when
            couponService.deleteCoupon(COUPON_ID);

            // then
            verify(saveCouponPort).delete(COUPON_ID);
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 삭제 시 예외")
        void deleteCouponNotFoundThrowsException() {
            // given
            given(loadCouponPort.findById(NON_EXISTENT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.deleteCoupon(NON_EXISTENT_ID))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    // =====================================================
    // 쿠폰 상태 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("쿠폰 상태 관리")
    class CouponStatus {

        @Test
        @DisplayName("쿠폰 활성화 성공")
        void activateCouponSuccess() {
            // given
            Coupon coupon = createCoupon();

            given(loadCouponPort.findById(COUPON_ID))
                    .willReturn(Optional.of(coupon));
            given(saveCouponPort.save(any(Coupon.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Coupon result = couponService.activateCoupon(COUPON_ID);

            // then
            assertThat(result.isActive()).isTrue();
        }

        @Test
        @DisplayName("쿠폰 비활성화 성공")
        void deactivateCouponSuccess() {
            // given
            Coupon coupon = createCoupon();

            given(loadCouponPort.findById(COUPON_ID))
                    .willReturn(Optional.of(coupon));
            given(saveCouponPort.save(any(Coupon.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Coupon result = couponService.deactivateCoupon(COUPON_ID);

            // then
            assertThat(result.isActive()).isFalse();
        }
    }

    // =====================================================
    // 쿠폰 사용 테스트
    // =====================================================

    @Nested
    @DisplayName("쿠폰 사용")
    class UseCoupon {

        @Test
        @DisplayName("쿠폰 사용 성공")
        void useCouponSuccess() {
            // given
            Coupon coupon = createCoupon();
            int initialUsed = coupon.getUsedQuantity();

            given(loadCouponPort.findById(COUPON_ID))
                    .willReturn(Optional.of(coupon));
            given(saveCouponPort.save(any(Coupon.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Coupon result = couponService.useCoupon(COUPON_ID);

            // then
            assertThat(result.getUsedQuantity()).isEqualTo(initialUsed + 1);
        }

        @Test
        @DisplayName("쿠폰 사용 취소 성공")
        void cancelCouponUsageSuccess() {
            // given
            Coupon coupon = createCoupon();
            coupon.use(); // 먼저 사용
            int usedAfterUse = coupon.getUsedQuantity();

            given(loadCouponPort.findById(COUPON_ID))
                    .willReturn(Optional.of(coupon));
            given(saveCouponPort.save(any(Coupon.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Coupon result = couponService.cancelCouponUsage(COUPON_ID);

            // then
            assertThat(result.getUsedQuantity()).isEqualTo(usedAfterUse - 1);
        }

        @Test
        @DisplayName("정액 할인 계산")
        void calculateFixedDiscount() {
            // given
            Coupon coupon = createCoupon();

            given(loadCouponPort.findByCode(COUPON_CODE))
                    .willReturn(Optional.of(coupon));

            // when
            BigDecimal discount = couponService.calculateDiscount(COUPON_CODE, new BigDecimal("50000"));

            // then
            assertThat(discount).isEqualByComparingTo(new BigDecimal("1000"));
        }

        @Test
        @DisplayName("정률 할인 계산 (최대 할인 적용)")
        void calculatePercentageDiscountWithMax() {
            // given
            Coupon coupon = createPercentageCoupon();

            given(loadCouponPort.findByCode("PERCENT10"))
                    .willReturn(Optional.of(coupon));

            // when - 100,000원의 10% = 10,000원이지만 최대 할인 5,000원 적용
            BigDecimal discount = couponService.calculateDiscount("PERCENT10", new BigDecimal("100000"));

            // then
            assertThat(discount).isEqualByComparingTo(new BigDecimal("5000"));
        }
    }

    // =====================================================
    // 쿠폰 조회 테스트
    // =====================================================

    @Nested
    @DisplayName("쿠폰 조회")
    class GetCoupon {

        @Test
        @DisplayName("ID로 쿠폰 조회 성공")
        void getCouponByIdSuccess() {
            // given
            Coupon coupon = createCoupon();

            given(loadCouponPort.findById(COUPON_ID))
                    .willReturn(Optional.of(coupon));

            // when
            Coupon result = couponService.getCoupon(COUPON_ID);

            // then
            assertThat(result.getId()).isEqualTo(COUPON_ID);
        }

        @Test
        @DisplayName("코드로 쿠폰 조회 성공")
        void getCouponByCodeSuccess() {
            // given
            Coupon coupon = createCoupon();

            given(loadCouponPort.findByCode(COUPON_CODE))
                    .willReturn(Optional.of(coupon));

            // when
            Coupon result = couponService.getCouponByCode(COUPON_CODE);

            // then
            assertThat(result.getCode()).isEqualTo(COUPON_CODE);
        }

        @Test
        @DisplayName("전체 쿠폰 조회")
        void getAllCoupons() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Coupon> coupons = new PageImpl<>(List.of(createCoupon()));

            given(loadCouponPort.findAll(pageable))
                    .willReturn(coupons);

            // when
            Page<Coupon> result = couponService.getAllCoupons(pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("활성 쿠폰 조회")
        void getActiveCoupons() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Coupon> coupons = new PageImpl<>(List.of(createCoupon()));

            given(loadCouponPort.findByActiveStatus(true, pageable))
                    .willReturn(coupons);

            // when
            Page<Coupon> result = couponService.getActiveCoupons(pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("사용 가능한 쿠폰 조회")
        void getUsableCoupons() {
            // given
            given(loadCouponPort.findUsableCoupons())
                    .willReturn(List.of(createCoupon()));

            // when
            List<Coupon> result = couponService.getUsableCoupons();

            // then
            assertThat(result).hasSize(1);
        }
    }

    // =====================================================
    // 쿠폰 유효성 검사 테스트
    // =====================================================

    @Nested
    @DisplayName("쿠폰 유효성 검사")
    class ValidateCoupon {

        @Test
        @DisplayName("유효한 쿠폰 검증 성공")
        void validateCouponSuccess() {
            // given
            Coupon coupon = createCoupon();

            given(loadCouponPort.findByCode(COUPON_CODE))
                    .willReturn(Optional.of(coupon));

            // when
            CouponValidationResult result = couponService.validateCoupon(COUPON_CODE, new BigDecimal("20000"));

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("1000"));
        }

        @Test
        @DisplayName("최소 주문 금액 미달 시 검증 실패")
        void validateCouponMinimumAmountFailed() {
            // given
            Coupon coupon = createCoupon();

            given(loadCouponPort.findByCode(COUPON_CODE))
                    .willReturn(Optional.of(coupon));

            // when - 최소 주문 금액 10,000원 미달
            CouponValidationResult result = couponService.validateCoupon(COUPON_CODE, new BigDecimal("5000"));

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.message()).contains("Minimum order amount");
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 코드 검증 실패")
        void validateCouponNotFoundFailed() {
            // given
            given(loadCouponPort.findByCode("INVALID"))
                    .willReturn(Optional.empty());

            // when
            CouponValidationResult result = couponService.validateCoupon("INVALID", new BigDecimal("20000"));

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.message()).isEqualTo("Invalid coupon code");
        }
    }
}

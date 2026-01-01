package jjh.delivery.application.service;

import jjh.delivery.application.port.in.ManageCouponUseCase;
import jjh.delivery.application.port.out.LoadCouponPort;
import jjh.delivery.application.port.out.SaveCouponPort;
import jjh.delivery.domain.promotion.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Coupon Service - Application Service
 * 쿠폰 관리 서비스
 */
@Service
@Transactional
public class CouponService implements ManageCouponUseCase {

    private final LoadCouponPort loadCouponPort;
    private final SaveCouponPort saveCouponPort;

    public CouponService(LoadCouponPort loadCouponPort, SaveCouponPort saveCouponPort) {
        this.loadCouponPort = loadCouponPort;
        this.saveCouponPort = saveCouponPort;
    }

    // ==================== 쿠폰 생성/수정/삭제 ====================

    @Override
    public Coupon createCoupon(CreateCouponCommand command) {
        // 코드 중복 체크
        if (loadCouponPort.findByCode(command.code()).isPresent()) {
            throw new IllegalArgumentException("Coupon code already exists: " + command.code());
        }

        Coupon coupon = Coupon.builder()
                .code(command.code())
                .name(command.name())
                .description(command.description())
                .discountType(command.discountType())
                .discountValue(command.discountValue())
                .minimumOrderAmount(command.minimumOrderAmount())
                .maximumDiscountAmount(command.maximumDiscountAmount())
                .scope(command.scope())
                .scopeTargetId(command.scopeTargetId())
                .totalQuantity(command.totalQuantity())
                .validFrom(command.validFrom())
                .validUntil(command.validUntil())
                .isActive(true)
                .build();

        return saveCouponPort.save(coupon);
    }

    @Override
    public Coupon updateCoupon(UpdateCouponCommand command) {
        Coupon existingCoupon = getCoupon(command.couponId());

        Coupon updatedCoupon = Coupon.builder()
                .id(existingCoupon.getId())
                .code(existingCoupon.getCode())
                .name(getOrDefault(command.name(), existingCoupon::getName))
                .description(getOrDefault(command.description(), existingCoupon::getDescription))
                .discountType(existingCoupon.getDiscountType())
                .discountValue(existingCoupon.getDiscountValue())
                .minimumOrderAmount(getOrDefault(command.minimumOrderAmount(), existingCoupon::getMinimumOrderAmount))
                .maximumDiscountAmount(getOrDefault(command.maximumDiscountAmount(), existingCoupon::getMaximumDiscountAmount))
                .scope(existingCoupon.getScope())
                .scopeTargetId(existingCoupon.getScopeTargetId())
                .totalQuantity(command.totalQuantity() > 0 ? command.totalQuantity() : existingCoupon.getTotalQuantity())
                .usedQuantity(existingCoupon.getUsedQuantity())
                .validFrom(getOrDefault(command.validFrom(), existingCoupon::getValidFrom))
                .validUntil(getOrDefault(command.validUntil(), existingCoupon::getValidUntil))
                .isActive(existingCoupon.isActive())
                .createdAt(existingCoupon.getCreatedAt())
                .build();

        return saveCouponPort.save(updatedCoupon);
    }

    /**
     * null이 아니면 새 값을 반환하고, null이면 기본값 공급자에서 값을 가져옴
     */
    private <T> T getOrDefault(T value, Supplier<T> defaultSupplier) {
        return Optional.ofNullable(value).orElseGet(defaultSupplier);
    }

    @Override
    public void deleteCoupon(String couponId) {
        getCoupon(couponId); // 존재 확인
        saveCouponPort.delete(couponId);
    }

    // ==================== 쿠폰 상태 관리 ====================

    @Override
    public Coupon activateCoupon(String couponId) {
        Coupon coupon = getCoupon(couponId);
        coupon.activate();
        return saveCouponPort.save(coupon);
    }

    @Override
    public Coupon deactivateCoupon(String couponId) {
        Coupon coupon = getCoupon(couponId);
        coupon.deactivate();
        return saveCouponPort.save(coupon);
    }

    // ==================== 쿠폰 사용 ====================

    @Override
    public Coupon useCoupon(String couponId) {
        Coupon coupon = getCoupon(couponId);
        coupon.use();
        return saveCouponPort.save(coupon);
    }

    @Override
    public Coupon cancelCouponUsage(String couponId) {
        Coupon coupon = getCoupon(couponId);
        coupon.cancelUse();
        return saveCouponPort.save(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(String couponCode, BigDecimal orderAmount) {
        Coupon coupon = getCouponByCode(couponCode);
        return coupon.calculateDiscount(orderAmount);
    }

    // ==================== 쿠폰 조회 ====================

    @Override
    @Transactional(readOnly = true)
    public Coupon getCoupon(String couponId) {
        return loadCouponPort.findById(couponId)
                .orElseThrow(() -> new NoSuchElementException("Coupon not found: " + couponId));
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon getCouponByCode(String code) {
        return loadCouponPort.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("Coupon not found with code: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Coupon> getAllCoupons(Pageable pageable) {
        return loadCouponPort.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Coupon> getActiveCoupons(Pageable pageable) {
        return loadCouponPort.findByActiveStatus(true, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Coupon> getUsableCoupons() {
        return loadCouponPort.findUsableCoupons();
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResult validateCoupon(String couponCode, BigDecimal orderAmount) {
        try {
            Coupon coupon = getCouponByCode(couponCode);

            if (!coupon.isUsable()) {
                return CouponValidationResult.failure("Coupon is not usable");
            }

            if (!coupon.isApplicable(orderAmount)) {
                return CouponValidationResult.failure(
                        String.format("Minimum order amount is %s", coupon.getMinimumOrderAmount())
                );
            }

            BigDecimal discountAmount = coupon.calculateDiscount(orderAmount);
            return CouponValidationResult.success(coupon, discountAmount);
        } catch (NoSuchElementException e) {
            return CouponValidationResult.failure("Invalid coupon code");
        }
    }
}

package jjh.delivery.adapter.in.web.coupon;

import jakarta.validation.Valid;
import jjh.delivery.adapter.in.web.coupon.dto.*;
import jjh.delivery.application.port.in.ManageCouponUseCase;
import jjh.delivery.application.port.in.ManageCouponUseCase.*;
import jjh.delivery.domain.promotion.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Coupon REST Controller - Driving Adapter (Inbound)
 * 쿠폰 관리 API
 */
@RestController
@RequestMapping("/api/v2/coupons")
public class CouponController {

    private final ManageCouponUseCase manageCouponUseCase;

    public CouponController(ManageCouponUseCase manageCouponUseCase) {
        this.manageCouponUseCase = manageCouponUseCase;
    }

    // ==================== Admin Endpoints ====================

    /**
     * 쿠폰 생성
     */
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(
            @Valid @RequestBody CreateCouponRequest request
    ) {
        CreateCouponCommand command = new CreateCouponCommand(
                request.code(),
                request.name(),
                request.description(),
                request.discountType(),
                request.discountValue(),
                request.minimumOrderAmount(),
                request.maximumDiscountAmount(),
                request.scope(),
                request.scopeTargetId(),
                request.totalQuantity(),
                request.validFrom(),
                request.validUntil()
        );

        Coupon coupon = manageCouponUseCase.createCoupon(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(CouponResponse.from(coupon));
    }

    /**
     * 쿠폰 수정
     */
    @PutMapping("/{couponId}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable String couponId,
            @Valid @RequestBody UpdateCouponRequest request
    ) {
        UpdateCouponCommand command = new UpdateCouponCommand(
                couponId,
                request.name(),
                request.description(),
                request.minimumOrderAmount(),
                request.maximumDiscountAmount(),
                request.totalQuantity(),
                request.validFrom(),
                request.validUntil()
        );

        Coupon coupon = manageCouponUseCase.updateCoupon(command);

        return ResponseEntity.ok(CouponResponse.from(coupon));
    }

    /**
     * 쿠폰 삭제
     */
    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCoupon(
            @PathVariable String couponId
    ) {
        manageCouponUseCase.deleteCoupon(couponId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 쿠폰 활성화
     */
    @PostMapping("/{couponId}/activate")
    public ResponseEntity<CouponResponse> activateCoupon(
            @PathVariable String couponId
    ) {
        Coupon coupon = manageCouponUseCase.activateCoupon(couponId);

        return ResponseEntity.ok(CouponResponse.from(coupon));
    }

    /**
     * 쿠폰 비활성화
     */
    @PostMapping("/{couponId}/deactivate")
    public ResponseEntity<CouponResponse> deactivateCoupon(
            @PathVariable String couponId
    ) {
        Coupon coupon = manageCouponUseCase.deactivateCoupon(couponId);

        return ResponseEntity.ok(CouponResponse.from(coupon));
    }

    // ==================== Query Endpoints ====================

    /**
     * 쿠폰 조회
     */
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponResponse> getCoupon(
            @PathVariable String couponId
    ) {
        Coupon coupon = manageCouponUseCase.getCoupon(couponId);

        return ResponseEntity.ok(CouponResponse.from(coupon));
    }

    /**
     * 쿠폰 코드로 조회
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(
            @PathVariable String code
    ) {
        Coupon coupon = manageCouponUseCase.getCouponByCode(code);

        return ResponseEntity.ok(CouponResponse.from(coupon));
    }

    /**
     * 전체 쿠폰 조회
     */
    @GetMapping
    public ResponseEntity<CouponListResponse> getAllCoupons(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Coupon> coupons = manageCouponUseCase.getAllCoupons(pageable);

        return ResponseEntity.ok(CouponListResponse.from(coupons));
    }

    /**
     * 활성 쿠폰 조회
     */
    @GetMapping("/active")
    public ResponseEntity<CouponListResponse> getActiveCoupons(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Coupon> coupons = manageCouponUseCase.getActiveCoupons(pageable);

        return ResponseEntity.ok(CouponListResponse.from(coupons));
    }

    /**
     * 사용 가능한 쿠폰 목록
     */
    @GetMapping("/usable")
    public ResponseEntity<List<CouponResponse>> getUsableCoupons() {
        List<Coupon> coupons = manageCouponUseCase.getUsableCoupons();

        List<CouponResponse> response = coupons.stream()
                .map(CouponResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    // ==================== Customer Endpoints ====================

    /**
     * 쿠폰 유효성 검사
     */
    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request
    ) {
        CouponValidationResult result = manageCouponUseCase.validateCoupon(
                request.couponCode(),
                request.orderAmount()
        );

        return ResponseEntity.ok(CouponValidationResponse.from(result));
    }

    /**
     * 할인 금액 계산
     */
    @GetMapping("/calculate")
    public ResponseEntity<CouponValidationResponse> calculateDiscount(
            @RequestParam String couponCode,
            @RequestParam java.math.BigDecimal orderAmount
    ) {
        CouponValidationResult result = manageCouponUseCase.validateCoupon(couponCode, orderAmount);

        return ResponseEntity.ok(CouponValidationResponse.from(result));
    }
}

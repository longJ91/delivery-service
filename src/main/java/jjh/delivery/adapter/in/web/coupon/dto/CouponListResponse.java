package jjh.delivery.adapter.in.web.coupon.dto;

import jjh.delivery.domain.promotion.Coupon;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 쿠폰 목록 응답
 */
public record CouponListResponse(
        List<CouponResponse> coupons,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static CouponListResponse from(Page<Coupon> page) {
        List<CouponResponse> coupons = page.getContent().stream()
                .map(CouponResponse::from)
                .toList();

        return new CouponListResponse(
                coupons,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}

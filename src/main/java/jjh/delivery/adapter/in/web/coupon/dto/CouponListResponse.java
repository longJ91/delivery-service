package jjh.delivery.adapter.in.web.coupon.dto;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.promotion.Coupon;

import java.util.List;

/**
 * 쿠폰 목록 응답 (커서 기반 페이지네이션)
 */
public record CouponListResponse(
        List<CouponResponse> content,
        int size,
        boolean hasNext,
        String nextCursor
) {
    public static CouponListResponse from(CursorPageResponse<Coupon> cursorPage) {
        List<CouponResponse> content = cursorPage.content().stream()
                .map(CouponResponse::from)
                .toList();

        return new CouponListResponse(
                content,
                cursorPage.size(),
                cursorPage.hasNext(),
                cursorPage.nextCursor()
        );
    }
}

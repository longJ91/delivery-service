package jjh.delivery.adapter.in.web.seller.dto;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.seller.Seller;

import java.util.List;

/**
 * 판매자 목록 응답 (커서 기반 페이지네이션)
 */
public record SellerListResponse(
        List<SellerResponse> content,
        int size,
        boolean hasNext,
        String nextCursor
) {
    public static SellerListResponse from(CursorPageResponse<Seller> cursorPage) {
        List<SellerResponse> content = cursorPage.content().stream()
                .map(SellerResponse::from)
                .toList();

        return new SellerListResponse(
                content,
                cursorPage.size(),
                cursorPage.hasNext(),
                cursorPage.nextCursor()
        );
    }
}

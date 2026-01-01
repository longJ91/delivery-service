package jjh.delivery.adapter.in.web.returns.dto;

import jjh.delivery.domain.returns.ProductReturn;

import java.util.List;

/**
 * 반품 목록 응답
 */
public record ReturnListResponse(
        List<ReturnResponse> returns,
        int totalCount
) {
    public static ReturnListResponse from(List<ProductReturn> returns) {
        List<ReturnResponse> items = returns.stream()
                .map(ReturnResponse::from)
                .toList();
        return new ReturnListResponse(items, items.size());
    }
}

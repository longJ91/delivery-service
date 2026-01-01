package jjh.delivery.adapter.in.web.seller.dto;

import jjh.delivery.domain.seller.Seller;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 판매자 목록 응답
 */
public record SellerListResponse(
        List<SellerResponse> sellers,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static SellerListResponse from(Page<Seller> page) {
        List<SellerResponse> sellers = page.getContent().stream()
                .map(SellerResponse::from)
                .toList();

        return new SellerListResponse(
                sellers,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}

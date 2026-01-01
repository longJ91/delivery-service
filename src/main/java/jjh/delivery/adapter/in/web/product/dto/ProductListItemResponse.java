package jjh.delivery.adapter.in.web.product.dto;

import jjh.delivery.domain.product.Product;

import java.math.BigDecimal;

/**
 * 상품 목록 아이템 응답
 */
public record ProductListItemResponse(
        String id,
        String name,
        BigDecimal price,
        BigDecimal discountPrice,
        String thumbnailUrl,
        double ratingAvg,
        long reviewCount,
        String sellerId,
        String sellerName
) {
    public static ProductListItemResponse from(Product product, double ratingAvg, long reviewCount, String sellerName) {
        String thumbnail = product.getImageUrls().isEmpty() ? null : product.getImageUrls().get(0);
        return new ProductListItemResponse(
                product.getId(),
                product.getName(),
                product.getBasePrice(),
                null, // discountPrice - 할인 기능 추가 시 구현
                thumbnail,
                ratingAvg,
                reviewCount,
                product.getSellerId(),
                sellerName
        );
    }
}

package jjh.delivery.application.port.out;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.product.Product;
import jjh.delivery.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Product 조회 Port - Driven Port (Outbound)
 * Note: countByCategoryId는 LoadProductStatsPort로 분리됨
 */
public interface LoadProductPort {

    /**
     * ID로 상품 조회
     */
    Optional<Product> findById(UUID productId);

    /**
     * 상품 목록 검색 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Product> searchProducts(SearchProductQuery query);

    /**
     * 판매자별 상품 목록 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Product> findBySellerId(UUID sellerId, ProductStatus status, String cursor, int size);

    /**
     * 상품 존재 여부 확인
     */
    boolean existsById(UUID productId);

    /**
     * 상품 검색 쿼리 (커서 기반)
     * @param cursor 이전 페이지의 마지막 커서 값 (첫 페이지는 null)
     * @param size 조회할 아이템 수
     */
    record SearchProductQuery(
            UUID categoryId,
            UUID sellerId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            List<ProductStatus> statuses,
            String cursor,
            int size
    ) {
        public SearchProductQuery {
            if (size <= 0) size = 20;
            if (size > 100) size = 100;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private UUID categoryId;
            private UUID sellerId;
            private String keyword;
            private BigDecimal minPrice;
            private BigDecimal maxPrice;
            private List<ProductStatus> statuses = List.of(ProductStatus.ACTIVE);
            private String cursor;
            private int size = 20;

            public Builder categoryId(UUID categoryId) {
                this.categoryId = categoryId;
                return this;
            }

            public Builder sellerId(UUID sellerId) {
                this.sellerId = sellerId;
                return this;
            }

            public Builder keyword(String keyword) {
                this.keyword = keyword;
                return this;
            }

            public Builder minPrice(BigDecimal minPrice) {
                this.minPrice = minPrice;
                return this;
            }

            public Builder maxPrice(BigDecimal maxPrice) {
                this.maxPrice = maxPrice;
                return this;
            }

            public Builder statuses(List<ProductStatus> statuses) {
                this.statuses = statuses;
                return this;
            }

            public Builder cursor(String cursor) {
                this.cursor = cursor;
                return this;
            }

            public Builder size(int size) {
                this.size = size;
                return this;
            }

            public SearchProductQuery build() {
                return new SearchProductQuery(categoryId, sellerId, keyword, minPrice, maxPrice, statuses, cursor, size);
            }
        }
    }
}

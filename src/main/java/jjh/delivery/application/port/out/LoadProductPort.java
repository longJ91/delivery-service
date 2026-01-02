package jjh.delivery.application.port.out;

import jjh.delivery.domain.product.Product;
import jjh.delivery.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product 조회 Port - Driven Port (Outbound)
 * Note: countByCategoryId는 LoadProductStatsPort로 분리됨
 */
public interface LoadProductPort {

    /**
     * ID로 상품 조회
     */
    Optional<Product> findById(String productId);

    /**
     * 상품 목록 검색
     */
    Page<Product> searchProducts(SearchProductQuery query, Pageable pageable);

    /**
     * 판매자별 상품 목록 조회
     */
    Page<Product> findBySellerId(String sellerId, ProductStatus status, Pageable pageable);

    /**
     * 상품 존재 여부 확인
     */
    boolean existsById(String productId);

    /**
     * 상품 검색 쿼리
     */
    record SearchProductQuery(
            String categoryId,
            String sellerId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            List<ProductStatus> statuses
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String categoryId;
            private String sellerId;
            private String keyword;
            private BigDecimal minPrice;
            private BigDecimal maxPrice;
            private List<ProductStatus> statuses = List.of(ProductStatus.ACTIVE);

            public Builder categoryId(String categoryId) {
                this.categoryId = categoryId;
                return this;
            }

            public Builder sellerId(String sellerId) {
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

            public SearchProductQuery build() {
                return new SearchProductQuery(categoryId, sellerId, keyword, minPrice, maxPrice, statuses);
            }
        }
    }
}

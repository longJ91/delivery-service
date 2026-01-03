package jjh.delivery.adapter.in.web.product;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.adapter.in.web.product.dto.*;
import jjh.delivery.application.port.out.LoadProductPort;
import jjh.delivery.application.port.out.LoadProductPort.SearchProductQuery;
import jjh.delivery.application.port.out.LoadReviewPort;
import jjh.delivery.application.port.out.LoadReviewStatsPort;
import jjh.delivery.application.port.out.LoadSellerInfoPort;
import jjh.delivery.application.port.out.LoadCustomerPort;
import jjh.delivery.domain.product.Product;
import jjh.delivery.domain.product.ProductStatus;
import jjh.delivery.domain.product.exception.ProductNotFoundException;
import jjh.delivery.domain.review.Review;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product REST Controller - Driving Adapter (Inbound)
 * 상품 조회 API (Public)
 */
@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
public class ProductController {

    private final LoadProductPort loadProductPort;
    private final LoadReviewPort loadReviewPort;
    private final LoadReviewStatsPort loadReviewStatsPort;
    private final LoadSellerInfoPort loadSellerInfoPort;
    private final LoadCustomerPort loadCustomerPort;

    /**
     * 상품 목록 조회 (커서 기반 페이지네이션)
     *
     * @param cursor 이전 페이지의 nextCursor 값 (첫 페이지는 생략)
     */
    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sellerId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        SearchProductQuery query = SearchProductQuery.builder()
                .categoryId(categoryId != null ? UUID.fromString(categoryId) : null)
                .sellerId(sellerId != null ? UUID.fromString(sellerId) : null)
                .keyword(keyword)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .statuses(List.of(ProductStatus.ACTIVE, ProductStatus.OUT_OF_STOCK))
                .cursor(cursor)
                .size(size)
                .build();

        CursorPageResponse<Product> products = loadProductPort.searchProducts(query);

        CursorPageResponse<ProductListItemResponse> responsePage = products.map(product -> {
            double ratingAvg = loadReviewStatsPort.getAverageRatingByProductId(product.getId());
            long reviewCount = loadReviewPort.countByProductId(product.getId());
            String sellerName = loadSellerInfoPort.findBusinessNameById(product.getSellerId()).orElse("Unknown");
            return ProductListItemResponse.from(product, ratingAvg, reviewCount, sellerName);
        });

        return ResponseEntity.ok(ProductListResponse.from(responsePage));
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable UUID productId) {
        Product product = loadProductPort.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString()));

        String sellerName = loadSellerInfoPort.findBusinessNameById(product.getSellerId()).orElse("Unknown");
        double ratingAvg = loadReviewStatsPort.getAverageRatingByProductId(productId);
        long reviewCount = loadReviewPort.countByProductId(productId);

        return ResponseEntity.ok(ProductDetailResponse.from(product, sellerName, ratingAvg, reviewCount));
    }

    /**
     * 상품 리뷰 목록 조회 (커서 기반 페이지네이션)
     *
     * @param cursor 이전 페이지의 nextCursor 값 (첫 페이지는 생략)
     */
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<ReviewListResponse> getProductReviews(
            @PathVariable UUID productId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 상품 존재 확인
        if (!loadProductPort.existsById(productId)) {
            throw new ProductNotFoundException(productId.toString());
        }

        CursorPageResponse<Review> reviews = loadReviewPort.findByProductId(productId, cursor, size);

        CursorPageResponse<ReviewResponse> responsePage = reviews.map(review -> {
            String customerName = loadCustomerPort.findById(review.getCustomerId())
                    .map(c -> c.getName())
                    .orElse("Unknown");
            return ReviewResponse.from(review, customerName);
        });

        Map<Integer, Long> ratingDistribution = loadReviewStatsPort.getRatingDistributionByProductId(productId);

        return ResponseEntity.ok(ReviewListResponse.from(responsePage, ratingDistribution));
    }
}

package jjh.delivery.adapter.in.web.product;

import jjh.delivery.adapter.in.web.product.dto.*;
import jjh.delivery.application.port.out.LoadProductPort;
import jjh.delivery.application.port.out.LoadProductPort.SearchProductQuery;
import jjh.delivery.application.port.out.LoadReviewPort;
import jjh.delivery.application.port.out.LoadSellerPort;
import jjh.delivery.application.port.out.LoadCustomerPort;
import jjh.delivery.domain.product.Product;
import jjh.delivery.domain.product.ProductStatus;
import jjh.delivery.domain.product.exception.ProductNotFoundException;
import jjh.delivery.domain.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Product REST Controller - Driving Adapter (Inbound)
 * 상품 조회 API (Public)
 */
@RestController
@RequestMapping("/api/v2/products")
public class ProductController {

    private final LoadProductPort loadProductPort;
    private final LoadReviewPort loadReviewPort;
    private final LoadSellerPort loadSellerPort;
    private final LoadCustomerPort loadCustomerPort;

    public ProductController(
            LoadProductPort loadProductPort,
            LoadReviewPort loadReviewPort,
            LoadSellerPort loadSellerPort,
            LoadCustomerPort loadCustomerPort
    ) {
        this.loadProductPort = loadProductPort;
        this.loadReviewPort = loadReviewPort;
        this.loadSellerPort = loadSellerPort;
        this.loadCustomerPort = loadCustomerPort;
    }

    /**
     * 상품 목록 조회
     */
    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sellerId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        SearchProductQuery query = SearchProductQuery.builder()
                .categoryId(categoryId)
                .sellerId(sellerId)
                .keyword(keyword)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .statuses(List.of(ProductStatus.ACTIVE, ProductStatus.OUT_OF_STOCK))
                .build();

        Pageable pageable = createPageable(sort, page, size);
        Page<Product> products = loadProductPort.searchProducts(query, pageable);

        Page<ProductListItemResponse> responsePage = products.map(product -> {
            double ratingAvg = loadReviewPort.getAverageRatingByProductId(product.getId());
            long reviewCount = loadReviewPort.countByProductId(product.getId());
            String sellerName = loadSellerPort.findBusinessNameById(product.getSellerId()).orElse("Unknown");
            return ProductListItemResponse.from(product, ratingAvg, reviewCount, sellerName);
        });

        return ResponseEntity.ok(ProductListResponse.from(responsePage));
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable String productId) {
        Product product = loadProductPort.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        String sellerName = loadSellerPort.findBusinessNameById(product.getSellerId()).orElse("Unknown");
        double ratingAvg = loadReviewPort.getAverageRatingByProductId(productId);
        long reviewCount = loadReviewPort.countByProductId(productId);

        return ResponseEntity.ok(ProductDetailResponse.from(product, sellerName, ratingAvg, reviewCount));
    }

    /**
     * 상품 리뷰 목록 조회
     */
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<ReviewListResponse> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 상품 존재 확인
        if (!loadProductPort.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        Pageable pageable = createReviewPageable(sort, page, size);
        Page<Review> reviews = loadReviewPort.findByProductId(productId, pageable);

        Page<ReviewResponse> responsePage = reviews.map(review -> {
            String customerName = loadCustomerPort.findById(review.getCustomerId())
                    .map(c -> c.getName())
                    .orElse("Unknown");
            return ReviewResponse.from(review, customerName);
        });

        Map<Integer, Long> ratingDistribution = loadReviewPort.getRatingDistributionByProductId(productId);

        return ResponseEntity.ok(ReviewListResponse.from(responsePage, ratingDistribution));
    }

    // ==================== Private Methods ====================

    private Pageable createPageable(String sort, int page, int size) {
        Sort sortSpec = switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "basePrice");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "basePrice");
            case "rating" -> Sort.by(Sort.Direction.DESC, "createdAt"); // 실제 구현에서는 별도 테이블 조인 필요
            case "sales" -> Sort.by(Sort.Direction.DESC, "createdAt"); // 실제 구현에서는 판매량 조인 필요
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // recent
        };
        return PageRequest.of(page, size, sortSpec);
    }

    private Pageable createReviewPageable(String sort, int page, int size) {
        Sort sortSpec = switch (sort) {
            case "rating_high" -> Sort.by(Sort.Direction.DESC, "rating");
            case "rating_low" -> Sort.by(Sort.Direction.ASC, "rating");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // recent
        };
        return PageRequest.of(page, size, sortSpec);
    }
}

package jjh.delivery.config.cache;

import java.time.Duration;

/**
 * Cache Names and TTL Configuration
 *
 * Cache Key Pattern: {domain}::{scope}::{identifier}
 * Examples:
 *   - products::id::550e8400-e29b-41d4-a716-446655440000
 *   - categories::all
 *   - sellers::id::123e4567-e89b-12d3-a456-426614174000
 */
public final class CacheNames {

    private CacheNames() {
        // Utility class
    }

    // ==================== Cache Names ====================

    /**
     * Product cache - 상품 정보 (1시간 TTL)
     * 조회 빈도가 높고 변경이 적음
     */
    public static final String PRODUCTS = "products";

    /**
     * Seller cache - 판매자 정보 (2시간 TTL)
     * 변경이 거의 없음
     */
    public static final String SELLERS = "sellers";

    /**
     * Category cache - 카테고리 정보 (24시간 TTL)
     * 거의 변경되지 않는 마스터 데이터
     */
    public static final String CATEGORIES = "categories";

    /**
     * Seller Info cache - 주문 생성 시 판매자 정보 (30분 TTL)
     * 주문 생성 중 빈번하게 조회됨
     */
    public static final String SELLER_INFO = "sellerInfo";

    // ==================== TTL Configuration ====================

    /**
     * Products cache TTL: 1 hour
     */
    public static final Duration PRODUCTS_TTL = Duration.ofHours(1);

    /**
     * Sellers cache TTL: 2 hours
     */
    public static final Duration SELLERS_TTL = Duration.ofHours(2);

    /**
     * Categories cache TTL: 24 hours
     */
    public static final Duration CATEGORIES_TTL = Duration.ofHours(24);

    /**
     * Seller Info cache TTL: 30 minutes
     */
    public static final Duration SELLER_INFO_TTL = Duration.ofMinutes(30);

    // ==================== Key Generator Names ====================

    /**
     * Entity ID based key generator
     */
    public static final String ENTITY_KEY_GENERATOR = "entityKeyGenerator";

    /**
     * Composite key generator for complex queries
     */
    public static final String COMPOSITE_KEY_GENERATOR = "compositeKeyGenerator";
}

package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.ReviewJpaEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Review JPA Repository
 */
public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, UUID> {

    /**
     * ID로 리뷰 조회 (images, reply fetch join)
     */
    @Query("SELECT DISTINCT r FROM ReviewJpaEntity r " +
            "LEFT JOIN FETCH r.images " +
            "LEFT JOIN FETCH r.reply " +
            "WHERE r.id = :id")
    Optional<ReviewJpaEntity> findByIdWithDetails(@Param("id") UUID id);

    // ==================== Cursor-based Pagination ====================

    /**
     * 상품별 리뷰 조회 (커서 기반, visible만)
     */
    @Query("SELECT r FROM ReviewJpaEntity r WHERE r.productId = :productId AND r.isVisible = true " +
            "AND (r.createdAt < :cursorCreatedAt OR (r.createdAt = :cursorCreatedAt AND r.id < :cursorId)) " +
            "ORDER BY r.createdAt DESC, r.id DESC")
    List<ReviewJpaEntity> findByProductIdWithCursor(
            @Param("productId") UUID productId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    default List<ReviewJpaEntity> findByProductIdWithCursor(
            UUID productId, LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return findByProductIdWithCursor(productId, cursorCreatedAt, cursorId, PageRequest.of(0, limit));
    }

    /**
     * 상품별 리뷰 조회 (첫 페이지, visible만)
     */
    @Query("SELECT r FROM ReviewJpaEntity r WHERE r.productId = :productId AND r.isVisible = true " +
            "ORDER BY r.createdAt DESC, r.id DESC")
    List<ReviewJpaEntity> findByProductIdOrderByCreatedAtDesc(
            @Param("productId") UUID productId,
            Pageable pageable);

    default List<ReviewJpaEntity> findByProductIdOrderByCreatedAtDesc(UUID productId, int limit) {
        return findByProductIdOrderByCreatedAtDesc(productId, PageRequest.of(0, limit));
    }

    /**
     * 고객별 리뷰 조회 (커서 기반)
     */
    @Query("SELECT r FROM ReviewJpaEntity r WHERE r.customerId = :customerId " +
            "AND (r.createdAt < :cursorCreatedAt OR (r.createdAt = :cursorCreatedAt AND r.id < :cursorId)) " +
            "ORDER BY r.createdAt DESC, r.id DESC")
    List<ReviewJpaEntity> findByCustomerIdWithCursor(
            @Param("customerId") UUID customerId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    default List<ReviewJpaEntity> findByCustomerIdWithCursor(
            UUID customerId, LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return findByCustomerIdWithCursor(customerId, cursorCreatedAt, cursorId, PageRequest.of(0, limit));
    }

    /**
     * 고객별 리뷰 조회 (첫 페이지)
     */
    @Query("SELECT r FROM ReviewJpaEntity r WHERE r.customerId = :customerId " +
            "ORDER BY r.createdAt DESC, r.id DESC")
    List<ReviewJpaEntity> findByCustomerIdOrderByCreatedAtDesc(
            @Param("customerId") UUID customerId,
            Pageable pageable);

    default List<ReviewJpaEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, int limit) {
        return findByCustomerIdOrderByCreatedAtDesc(customerId, PageRequest.of(0, limit));
    }

    /**
     * 판매자별 리뷰 조회 (커서 기반)
     */
    @Query("SELECT r FROM ReviewJpaEntity r WHERE r.sellerId = :sellerId " +
            "AND (r.createdAt < :cursorCreatedAt OR (r.createdAt = :cursorCreatedAt AND r.id < :cursorId)) " +
            "ORDER BY r.createdAt DESC, r.id DESC")
    List<ReviewJpaEntity> findBySellerIdWithCursor(
            @Param("sellerId") UUID sellerId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    default List<ReviewJpaEntity> findBySellerIdWithCursor(
            UUID sellerId, LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return findBySellerIdWithCursor(sellerId, cursorCreatedAt, cursorId, PageRequest.of(0, limit));
    }

    /**
     * 판매자별 리뷰 조회 (첫 페이지)
     */
    @Query("SELECT r FROM ReviewJpaEntity r WHERE r.sellerId = :sellerId " +
            "ORDER BY r.createdAt DESC, r.id DESC")
    List<ReviewJpaEntity> findBySellerIdOrderByCreatedAtDesc(
            @Param("sellerId") UUID sellerId,
            Pageable pageable);

    default List<ReviewJpaEntity> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, int limit) {
        return findBySellerIdOrderByCreatedAtDesc(sellerId, PageRequest.of(0, limit));
    }

    // ==================== Count & Exists ====================

    /**
     * 상품별 리뷰 수
     */
    long countByProductIdAndIsVisibleTrue(UUID productId);

    /**
     * 주문에 대한 리뷰 존재 여부
     */
    boolean existsByOrderId(UUID orderId);

    // Note: getAverageRatingByProductId, getRatingDistributionByProductId는
    //       ReviewJooqRepository로 마이그레이션됨 (컴파일 타임 타입 안전성)
}

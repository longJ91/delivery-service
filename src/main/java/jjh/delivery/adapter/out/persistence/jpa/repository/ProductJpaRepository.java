package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import jjh.delivery.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Product JPA Repository
 */
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID>, JpaSpecificationExecutor<ProductJpaEntity> {

    /**
     * ID로 상품 조회 (variants fetch join)
     */
    @Query("SELECT DISTINCT p FROM ProductJpaEntity p LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Optional<ProductJpaEntity> findByIdWithVariants(@Param("id") UUID id);

    /**
     * 판매자별 상품 조회
     */
    Page<ProductJpaEntity> findBySellerIdAndStatus(UUID sellerId, ProductStatus status, Pageable pageable);

    /**
     * 판매자별 전체 상품 조회
     */
    Page<ProductJpaEntity> findBySellerId(UUID sellerId, Pageable pageable);

    // ==================== Cursor-based Pagination ====================

    /**
     * 판매자별 상품 조회 (커서 기반, 상태 필터링)
     */
    @Query("SELECT p FROM ProductJpaEntity p WHERE p.sellerId = :sellerId AND p.status = :status " +
            "AND (p.createdAt < :cursorCreatedAt OR (p.createdAt = :cursorCreatedAt AND p.id < :cursorId)) " +
            "ORDER BY p.createdAt DESC, p.id DESC")
    List<ProductJpaEntity> findBySellerIdAndStatusWithCursor(
            @Param("sellerId") UUID sellerId,
            @Param("status") ProductStatus status,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    default List<ProductJpaEntity> findBySellerIdAndStatusWithCursor(
            UUID sellerId, ProductStatus status, LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return findBySellerIdAndStatusWithCursor(sellerId, status, cursorCreatedAt, cursorId,
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    /**
     * 판매자별 상품 조회 (커서 기반, 상태 필터링 없음)
     */
    @Query("SELECT p FROM ProductJpaEntity p WHERE p.sellerId = :sellerId " +
            "AND (p.createdAt < :cursorCreatedAt OR (p.createdAt = :cursorCreatedAt AND p.id < :cursorId)) " +
            "ORDER BY p.createdAt DESC, p.id DESC")
    List<ProductJpaEntity> findBySellerIdWithCursor(
            @Param("sellerId") UUID sellerId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    default List<ProductJpaEntity> findBySellerIdWithCursor(
            UUID sellerId, LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return findBySellerIdWithCursor(sellerId, cursorCreatedAt, cursorId,
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    /**
     * 판매자별 상품 조회 (첫 페이지, 상태 필터링)
     */
    @Query("SELECT p FROM ProductJpaEntity p WHERE p.sellerId = :sellerId AND p.status = :status " +
            "ORDER BY p.createdAt DESC, p.id DESC")
    List<ProductJpaEntity> findBySellerIdAndStatusOrderByCreatedAtDesc(
            @Param("sellerId") UUID sellerId,
            @Param("status") ProductStatus status,
            Pageable pageable);

    default List<ProductJpaEntity> findBySellerIdAndStatusOrderByCreatedAtDesc(
            UUID sellerId, ProductStatus status, int limit) {
        return findBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, status,
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    /**
     * 판매자별 상품 조회 (첫 페이지, 상태 필터링 없음)
     */
    @Query("SELECT p FROM ProductJpaEntity p WHERE p.sellerId = :sellerId " +
            "ORDER BY p.createdAt DESC, p.id DESC")
    List<ProductJpaEntity> findBySellerIdOrderByCreatedAtDesc(
            @Param("sellerId") UUID sellerId,
            Pageable pageable);

    default List<ProductJpaEntity> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, int limit) {
        return findBySellerIdOrderByCreatedAtDesc(sellerId,
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    // Note: countByCategoryIdAndActive는 ProductJooqRepository로 마이그레이션됨 (컴파일 타임 타입 안전성)
}

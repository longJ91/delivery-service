package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.CouponJpaEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Coupon JPA Repository
 */
@Repository
public interface CouponJpaRepository extends JpaRepository<CouponJpaEntity, UUID> {

    Optional<CouponJpaEntity> findByCode(String code);

    // ==================== Cursor-based Pagination ====================

    /**
     * 전체 쿠폰 조회 (커서 기반)
     */
    @Query("SELECT c FROM CouponJpaEntity c " +
            "WHERE (c.createdAt < :cursorCreatedAt OR (c.createdAt = :cursorCreatedAt AND c.id < :cursorId)) " +
            "ORDER BY c.createdAt DESC, c.id DESC")
    List<CouponJpaEntity> findAllWithCursor(
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    default List<CouponJpaEntity> findAllWithCursor(LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return findAllWithCursor(cursorCreatedAt, cursorId, PageRequest.of(0, limit));
    }

    /**
     * 전체 쿠폰 조회 (첫 페이지)
     */
    @Query("SELECT c FROM CouponJpaEntity c ORDER BY c.createdAt DESC, c.id DESC")
    List<CouponJpaEntity> findAllOrderByCreatedAtDesc(Pageable pageable);

    default List<CouponJpaEntity> findAllOrderByCreatedAtDesc(int limit) {
        return findAllOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    /**
     * 활성 상태별 쿠폰 조회 (커서 기반)
     */
    @Query("SELECT c FROM CouponJpaEntity c WHERE c.isActive = :isActive " +
            "AND (c.createdAt < :cursorCreatedAt OR (c.createdAt = :cursorCreatedAt AND c.id < :cursorId)) " +
            "ORDER BY c.createdAt DESC, c.id DESC")
    List<CouponJpaEntity> findByIsActiveWithCursor(
            @Param("isActive") boolean isActive,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    default List<CouponJpaEntity> findByIsActiveWithCursor(
            boolean isActive, LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return findByIsActiveWithCursor(isActive, cursorCreatedAt, cursorId, PageRequest.of(0, limit));
    }

    /**
     * 활성 상태별 쿠폰 조회 (첫 페이지)
     */
    @Query("SELECT c FROM CouponJpaEntity c WHERE c.isActive = :isActive " +
            "ORDER BY c.createdAt DESC, c.id DESC")
    List<CouponJpaEntity> findByIsActiveOrderByCreatedAtDesc(
            @Param("isActive") boolean isActive,
            Pageable pageable);

    default List<CouponJpaEntity> findByIsActiveOrderByCreatedAtDesc(boolean isActive, int limit) {
        return findByIsActiveOrderByCreatedAtDesc(isActive, PageRequest.of(0, limit));
    }

    // ==================== Usable Coupons ====================

    @Query("SELECT c FROM CouponJpaEntity c WHERE c.isActive = true " +
            "AND (c.validFrom IS NULL OR c.validFrom <= :now) " +
            "AND (c.validUntil IS NULL OR c.validUntil >= :now) " +
            "AND (c.totalQuantity = 0 OR c.usedQuantity < c.totalQuantity)")
    List<CouponJpaEntity> findUsableCoupons(@Param("now") LocalDateTime now);
}

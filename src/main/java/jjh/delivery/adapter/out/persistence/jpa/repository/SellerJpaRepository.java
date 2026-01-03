package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.SellerJpaEntity;
import jjh.delivery.domain.seller.SellerStatus;
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
 * Seller JPA Repository
 * Note: findBusinessNameById는 SellerJooqRepository로 마이그레이션됨 (컴파일 타임 타입 안전성)
 */
@Repository
public interface SellerJpaRepository extends JpaRepository<SellerJpaEntity, UUID> {

    Optional<SellerJpaEntity> findByBusinessNumber(String businessNumber);

    Optional<SellerJpaEntity> findByEmail(String email);

    boolean existsByBusinessNumber(String businessNumber);

    boolean existsByEmail(String email);

    // ==================== Cursor-based Pagination ====================

    /**
     * 전체 판매자 조회 (커서 기반)
     */
    @Query("SELECT s FROM SellerJpaEntity s " +
            "WHERE (s.createdAt < :cursorCreatedAt OR (s.createdAt = :cursorCreatedAt AND s.id < :cursorId)) " +
            "ORDER BY s.createdAt DESC, s.id DESC")
    List<SellerJpaEntity> findAllWithCursor(
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    default List<SellerJpaEntity> findAllWithCursor(LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return findAllWithCursor(cursorCreatedAt, cursorId, PageRequest.of(0, limit));
    }

    /**
     * 전체 판매자 조회 (첫 페이지)
     */
    @Query("SELECT s FROM SellerJpaEntity s ORDER BY s.createdAt DESC, s.id DESC")
    List<SellerJpaEntity> findAllOrderByCreatedAtDesc(Pageable pageable);

    default List<SellerJpaEntity> findAllOrderByCreatedAtDesc(int limit) {
        return findAllOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    /**
     * 상태별 판매자 조회 (커서 기반)
     */
    @Query("SELECT s FROM SellerJpaEntity s WHERE s.status = :status " +
            "AND (s.createdAt < :cursorCreatedAt OR (s.createdAt = :cursorCreatedAt AND s.id < :cursorId)) " +
            "ORDER BY s.createdAt DESC, s.id DESC")
    List<SellerJpaEntity> findByStatusWithCursor(
            @Param("status") SellerStatus status,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    default List<SellerJpaEntity> findByStatusWithCursor(
            SellerStatus status, LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return findByStatusWithCursor(status, cursorCreatedAt, cursorId, PageRequest.of(0, limit));
    }

    /**
     * 상태별 판매자 조회 (첫 페이지)
     */
    @Query("SELECT s FROM SellerJpaEntity s WHERE s.status = :status " +
            "ORDER BY s.createdAt DESC, s.id DESC")
    List<SellerJpaEntity> findByStatusOrderByCreatedAtDesc(
            @Param("status") SellerStatus status,
            Pageable pageable);

    default List<SellerJpaEntity> findByStatusOrderByCreatedAtDesc(SellerStatus status, int limit) {
        return findByStatusOrderByCreatedAtDesc(status, PageRequest.of(0, limit));
    }
}

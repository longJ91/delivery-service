package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.ReviewJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Review JPA Repository
 */
public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, String> {

    /**
     * ID로 리뷰 조회 (images, reply fetch join)
     */
    @Query("SELECT DISTINCT r FROM ReviewJpaEntity r " +
            "LEFT JOIN FETCH r.images " +
            "LEFT JOIN FETCH r.reply " +
            "WHERE r.id = :id")
    Optional<ReviewJpaEntity> findByIdWithDetails(@Param("id") String id);

    /**
     * 상품별 리뷰 조회 (visible만)
     */
    Page<ReviewJpaEntity> findByProductIdAndIsVisibleTrue(String productId, Pageable pageable);

    /**
     * 고객별 리뷰 조회
     */
    Page<ReviewJpaEntity> findByCustomerId(String customerId, Pageable pageable);

    /**
     * 판매자별 리뷰 조회
     */
    Page<ReviewJpaEntity> findBySellerId(String sellerId, Pageable pageable);

    /**
     * 상품별 평균 평점
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM ReviewJpaEntity r WHERE r.productId = :productId AND r.isVisible = true")
    double getAverageRatingByProductId(@Param("productId") String productId);

    /**
     * 상품별 리뷰 수
     */
    long countByProductIdAndIsVisibleTrue(String productId);

    /**
     * 상품별 평점 분포
     */
    @Query("SELECT r.rating, COUNT(r) FROM ReviewJpaEntity r WHERE r.productId = :productId AND r.isVisible = true GROUP BY r.rating")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") String productId);

    /**
     * 주문에 대한 리뷰 존재 여부
     */
    boolean existsByOrderId(String orderId);
}

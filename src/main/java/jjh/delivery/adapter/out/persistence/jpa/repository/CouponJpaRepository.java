package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.CouponJpaEntity;
import org.springframework.data.domain.Page;
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

    Page<CouponJpaEntity> findByIsActive(boolean isActive, Pageable pageable);

    @Query("SELECT c FROM CouponJpaEntity c WHERE c.isActive = true " +
            "AND (c.validFrom IS NULL OR c.validFrom <= :now) " +
            "AND (c.validUntil IS NULL OR c.validUntil >= :now) " +
            "AND (c.totalQuantity = 0 OR c.usedQuantity < c.totalQuantity)")
    List<CouponJpaEntity> findUsableCoupons(@Param("now") LocalDateTime now);
}

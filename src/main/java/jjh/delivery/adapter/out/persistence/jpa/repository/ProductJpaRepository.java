package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import jjh.delivery.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Note: countByCategoryIdAndActive는 ProductJooqRepository로 마이그레이션됨 (컴파일 타임 타입 안전성)
}

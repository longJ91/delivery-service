package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.SellerJpaEntity;
import jjh.delivery.domain.seller.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    Page<SellerJpaEntity> findByStatus(SellerStatus status, Pageable pageable);

    boolean existsByBusinessNumber(String businessNumber);

    boolean existsByEmail(String email);
}

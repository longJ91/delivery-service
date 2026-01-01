package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.SellerJpaEntity;
import jjh.delivery.domain.seller.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Seller JPA Repository
 */
@Repository
public interface SellerJpaRepository extends JpaRepository<SellerJpaEntity, String> {

    /**
     * ID로 판매자 이름 조회
     */
    @Query("SELECT s.businessName FROM SellerJpaEntity s WHERE s.id = :id")
    Optional<String> findBusinessNameById(@Param("id") String id);

    Optional<SellerJpaEntity> findByBusinessNumber(String businessNumber);

    Optional<SellerJpaEntity> findByEmail(String email);

    Page<SellerJpaEntity> findByStatus(SellerStatus status, Pageable pageable);

    boolean existsByBusinessNumber(String businessNumber);

    boolean existsByEmail(String email);
}

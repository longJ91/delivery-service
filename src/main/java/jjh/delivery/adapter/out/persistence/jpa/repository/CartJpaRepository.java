package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Cart JPA Repository
 */
public interface CartJpaRepository extends JpaRepository<CartJpaEntity, UUID> {

    /**
     * 고객 ID로 장바구니 조회 (items fetch join)
     */
    @Query("SELECT DISTINCT c FROM CartJpaEntity c LEFT JOIN FETCH c.items WHERE c.customerId = :customerId")
    Optional<CartJpaEntity> findByCustomerIdWithItems(@Param("customerId") UUID customerId);

    /**
     * 고객 ID로 장바구니 존재 여부 확인
     */
    boolean existsByCustomerId(UUID customerId);

    /**
     * 고객 ID로 장바구니 삭제
     */
    void deleteByCustomerId(UUID customerId);
}

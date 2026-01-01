package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import jjh.delivery.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Order JPA Repository
 */
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {

    @Query("SELECT o FROM OrderJpaEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderJpaEntity> findByIdWithItems(@Param("id") String id);

    @Query("SELECT o FROM OrderJpaEntity o LEFT JOIN FETCH o.items WHERE o.customerId = :customerId ORDER BY o.createdAt DESC")
    List<OrderJpaEntity> findByCustomerIdWithItems(@Param("customerId") String customerId);

    @Query("SELECT o FROM OrderJpaEntity o LEFT JOIN FETCH o.items WHERE o.shopId = :shopId ORDER BY o.createdAt DESC")
    List<OrderJpaEntity> findByShopIdWithItems(@Param("shopId") String shopId);

    List<OrderJpaEntity> findByStatus(OrderStatus status);

    List<OrderJpaEntity> findByCustomerId(String customerId);

    List<OrderJpaEntity> findByShopId(String shopId);
}

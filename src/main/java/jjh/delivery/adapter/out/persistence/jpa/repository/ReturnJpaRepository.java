package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.ReturnJpaEntity;
import jjh.delivery.domain.returns.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Return JPA Repository
 */
public interface ReturnJpaRepository extends JpaRepository<ReturnJpaEntity, UUID> {

    List<ReturnJpaEntity> findByOrderId(UUID orderId);

    List<ReturnJpaEntity> findByCustomerId(UUID customerId);

    List<ReturnJpaEntity> findByStatus(ReturnStatus status);
}

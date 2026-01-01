package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.ReturnJpaEntity;
import jjh.delivery.domain.returns.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Return JPA Repository
 */
public interface ReturnJpaRepository extends JpaRepository<ReturnJpaEntity, String> {

    List<ReturnJpaEntity> findByOrderId(String orderId);

    List<ReturnJpaEntity> findByCustomerId(String customerId);

    List<ReturnJpaEntity> findByStatus(ReturnStatus status);
}

package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.PaymentJpaEntity;
import jjh.delivery.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Payment JPA Repository
 */
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, String> {

    Optional<PaymentJpaEntity> findByOrderId(String orderId);

    List<PaymentJpaEntity> findByOrderIdIn(List<String> orderIds);

    List<PaymentJpaEntity> findByStatus(PaymentStatus status);

    boolean existsByOrderId(String orderId);
}

package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.PaymentJpaEntity;
import jjh.delivery.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment JPA Repository
 */
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByOrderId(UUID orderId);

    List<PaymentJpaEntity> findByOrderIdIn(List<UUID> orderIds);

    List<PaymentJpaEntity> findByStatus(PaymentStatus status);

    boolean existsByOrderId(UUID orderId);
}

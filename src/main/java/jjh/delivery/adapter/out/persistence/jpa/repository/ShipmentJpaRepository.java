package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.ShipmentJpaEntity;
import jjh.delivery.domain.shipment.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Shipment JPA Repository
 */
public interface ShipmentJpaRepository extends JpaRepository<ShipmentJpaEntity, String> {

    Optional<ShipmentJpaEntity> findByOrderId(String orderId);

    Optional<ShipmentJpaEntity> findByTrackingNumber(String trackingNumber);

    List<ShipmentJpaEntity> findByStatus(ShipmentStatus status);

    boolean existsByOrderId(String orderId);
}

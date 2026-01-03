package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.ShipmentJpaEntity;
import jjh.delivery.domain.shipment.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Shipment JPA Repository
 */
public interface ShipmentJpaRepository extends JpaRepository<ShipmentJpaEntity, UUID> {

    Optional<ShipmentJpaEntity> findByOrderId(UUID orderId);

    Optional<ShipmentJpaEntity> findByTrackingNumber(String trackingNumber);

    List<ShipmentJpaEntity> findByStatus(ShipmentStatus status);

    boolean existsByOrderId(UUID orderId);
}

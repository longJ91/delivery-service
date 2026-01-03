package jjh.delivery.adapter.out.persistence.jpa.adapter;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.out.persistence.jpa.entity.ShipmentJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.ShipmentPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.ShipmentJpaRepository;
import jjh.delivery.application.port.out.LoadShipmentPort;
import jjh.delivery.application.port.out.SaveShipmentPort;
import jjh.delivery.domain.shipment.Shipment;
import jjh.delivery.domain.shipment.ShipmentStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Shipment JPA Adapter - Driven Adapter (Outbound)
 */
@Component
@RequiredArgsConstructor
public class ShipmentJpaAdapter implements LoadShipmentPort, SaveShipmentPort {

    private final ShipmentJpaRepository shipmentJpaRepository;
    private final ShipmentPersistenceMapper mapper;

    @Override
    public Optional<Shipment> findById(UUID shipmentId) {
        return shipmentJpaRepository.findById(shipmentId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByOrderId(UUID orderId) {
        return shipmentJpaRepository.findByOrderId(orderId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return shipmentJpaRepository.findByTrackingNumber(trackingNumber)
                .map(mapper::toDomain);
    }

    @Override
    public List<Shipment> findByStatus(ShipmentStatus status) {
        return shipmentJpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return shipmentJpaRepository.existsByOrderId(orderId);
    }

    @Override
    public Shipment save(Shipment shipment) {
        ShipmentJpaEntity entity = mapper.toEntity(shipment);
        ShipmentJpaEntity saved = shipmentJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}

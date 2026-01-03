package jjh.delivery.adapter.out.persistence.jpa.adapter;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.out.persistence.jpa.entity.ReturnJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.ReturnPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.ReturnJpaRepository;
import jjh.delivery.application.port.out.LoadReturnPort;
import jjh.delivery.application.port.out.SaveReturnPort;
import jjh.delivery.domain.returns.ProductReturn;
import jjh.delivery.domain.returns.ReturnStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Return JPA Adapter - Driven Adapter (Outbound)
 */
@Component
@RequiredArgsConstructor
public class ReturnJpaAdapter implements LoadReturnPort, SaveReturnPort {

    private final ReturnJpaRepository returnJpaRepository;
    private final ReturnPersistenceMapper mapper;

    @Override
    public Optional<ProductReturn> findById(UUID returnId) {
        return returnJpaRepository.findById(returnId)
                .map(mapper::toDomain);
    }

    @Override
    public List<ProductReturn> findByOrderId(UUID orderId) {
        return returnJpaRepository.findByOrderId(orderId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ProductReturn> findByCustomerId(UUID customerId) {
        return returnJpaRepository.findByCustomerId(customerId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ProductReturn> findByStatus(ReturnStatus status) {
        return returnJpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID returnId) {
        return returnJpaRepository.existsById(returnId);
    }

    @Override
    public ProductReturn save(ProductReturn productReturn) {
        ReturnJpaEntity entity = mapper.toEntity(productReturn);
        ReturnJpaEntity saved = returnJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}

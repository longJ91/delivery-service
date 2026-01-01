package jjh.delivery.adapter.out.persistence.jpa.adapter;

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

/**
 * Return JPA Adapter - Driven Adapter (Outbound)
 */
@Component
public class ReturnJpaAdapter implements LoadReturnPort, SaveReturnPort {

    private final ReturnJpaRepository returnJpaRepository;
    private final ReturnPersistenceMapper mapper;

    public ReturnJpaAdapter(
            ReturnJpaRepository returnJpaRepository,
            ReturnPersistenceMapper mapper
    ) {
        this.returnJpaRepository = returnJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ProductReturn> findById(String returnId) {
        return returnJpaRepository.findById(returnId)
                .map(mapper::toDomain);
    }

    @Override
    public List<ProductReturn> findByOrderId(String orderId) {
        return returnJpaRepository.findByOrderId(orderId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ProductReturn> findByCustomerId(String customerId) {
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
    public boolean existsById(String returnId) {
        return returnJpaRepository.existsById(returnId);
    }

    @Override
    public ProductReturn save(ProductReturn productReturn) {
        ReturnJpaEntity entity = mapper.toEntity(productReturn);
        ReturnJpaEntity saved = returnJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}

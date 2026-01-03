package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.OrderPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.OrderJpaRepository;
import jjh.delivery.application.port.out.LoadOrderPort;
import jjh.delivery.application.port.out.SaveOrderPort;
import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Order JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 주문 저장/조회 구현
 * Note: 조인 쿼리는 JPA @Query 사용 (OrderJooqAdapter에서 복잡한 쿼리 처리)
 */
@Component
@RequiredArgsConstructor
public class OrderJpaAdapter implements LoadOrderPort, SaveOrderPort {

    private final OrderJpaRepository repository;
    private final OrderPersistenceMapper mapper;

    // ==================== LoadOrderPort ====================

    @Override
    public Optional<Order> findById(UUID orderId) {
        return repository.findByIdWithItems(orderId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return mapper.toDomainList(repository.findByCustomerIdWithItems(customerId));
    }

    @Override
    public List<Order> findBySellerId(UUID sellerId) {
        return mapper.toDomainList(repository.findBySellerIdWithItems(sellerId));
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return mapper.toDomainList(repository.findByStatus(status));
    }

    @Override
    public boolean existsById(UUID orderId) {
        return repository.existsById(orderId);
    }

    // ==================== SaveOrderPort ====================

    @Override
    public Order save(Order order) {
        // 기존 엔티티가 있으면 업데이트, 없으면 새로 생성
        OrderJpaEntity entity = repository.findById(order.getId())
                .map(existing -> updateEntity(existing, order))
                .orElseGet(() -> mapper.toEntity(order));

        OrderJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(UUID orderId) {
        repository.deleteById(orderId);
    }

    private OrderJpaEntity updateEntity(OrderJpaEntity entity, Order order) {
        entity.setStatus(order.getStatus());
        entity.setTotalAmount(order.getTotalAmount());
        entity.setUpdatedAt(order.getUpdatedAt());
        return entity;
    }
}

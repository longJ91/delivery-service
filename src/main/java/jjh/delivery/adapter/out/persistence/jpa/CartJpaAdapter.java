package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.out.persistence.jpa.entity.CartItemJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.CartJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.CartPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.CartJpaRepository;
import jjh.delivery.application.port.out.LoadCartPort;
import jjh.delivery.application.port.out.SaveCartPort;
import jjh.delivery.domain.cart.Cart;
import jjh.delivery.domain.cart.CartItem;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Cart JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 장바구니 저장/조회 구현
 */
@Component
@RequiredArgsConstructor
public class CartJpaAdapter implements LoadCartPort, SaveCartPort {

    private final CartJpaRepository repository;
    private final CartPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findByCustomerId(String customerId) {
        // JPA 사용 (조인 쿼리는 JPA가 관계 매핑 처리)
        return repository.findByCustomerIdWithItems(customerId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByCustomerId(String customerId) {
        return repository.existsByCustomerId(customerId);
    }

    @Override
    @Transactional
    public Cart save(Cart cart) {
        Optional<CartJpaEntity> existing = repository.findByCustomerIdWithItems(cart.getCustomerId());

        if (existing.isPresent()) {
            CartJpaEntity entity = existing.get();
            syncItems(entity, cart);
            entity.setUpdatedAt(cart.getUpdatedAt());
            CartJpaEntity saved = repository.save(entity);
            return mapper.toDomain(saved);
        }

        CartJpaEntity entity = mapper.toEntity(cart);
        CartJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteByCustomerId(String customerId) {
        repository.deleteByCustomerId(customerId);
    }

    private void syncItems(CartJpaEntity entity, Cart cart) {
        // 기존 아이템 모두 삭제
        entity.clearItems();

        // 새 아이템 추가
        for (CartItem item : cart.getItems()) {
            entity.addItem(new CartItemJpaEntity(
                    item.id(),
                    item.productId(),
                    item.productName(),
                    item.variantId(),
                    item.variantName(),
                    item.sellerId(),
                    item.quantity(),
                    item.unitPrice(),
                    item.thumbnailUrl(),
                    item.addedAt()
            ));
        }
    }
}

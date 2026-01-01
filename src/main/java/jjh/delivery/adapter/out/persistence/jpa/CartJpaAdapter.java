package jjh.delivery.adapter.out.persistence.jpa;

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
 */
@Component
public class CartJpaAdapter implements LoadCartPort, SaveCartPort {

    private final CartJpaRepository repository;
    private final CartPersistenceMapper mapper;

    public CartJpaAdapter(CartJpaRepository repository, CartPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findByCustomerId(String customerId) {
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
            entity.addItem(new jjh.delivery.adapter.out.persistence.jpa.entity.CartItemJpaEntity(
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

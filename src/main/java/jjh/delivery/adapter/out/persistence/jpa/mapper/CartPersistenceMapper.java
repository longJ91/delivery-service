package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.CartItemJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.CartJpaEntity;
import jjh.delivery.domain.cart.Cart;
import jjh.delivery.domain.cart.CartItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Cart 영속성 매퍼
 */
@Component
public class CartPersistenceMapper {

    public Cart toDomain(CartJpaEntity entity) {
        return Cart.restore(
                entity.getId(),
                entity.getCustomerId(),
                entity.getItems().stream()
                        .map(this::toDomainItem)
                        .collect(Collectors.toList()),
                entity.getCreatedAt()
        );
    }

    private CartItem toDomainItem(CartItemJpaEntity entity) {
        return new CartItem(
                entity.getId(),
                entity.getProductId(),
                entity.getProductName(),
                entity.getVariantId(),
                entity.getVariantName(),
                entity.getSellerId(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getThumbnailUrl(),
                entity.getAddedAt()
        );
    }

    public CartJpaEntity toEntity(Cart domain) {
        CartJpaEntity entity = new CartJpaEntity(
                domain.getId(),
                domain.getCustomerId(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );

        for (CartItem item : domain.getItems()) {
            entity.addItem(toEntityItem(item));
        }

        return entity;
    }

    private CartItemJpaEntity toEntityItem(CartItem domain) {
        return new CartItemJpaEntity(
                domain.id(),
                domain.productId(),
                domain.productName(),
                domain.variantId(),
                domain.variantName(),
                domain.sellerId(),
                domain.quantity(),
                domain.unitPrice(),
                domain.thumbnailUrl(),
                domain.addedAt()
        );
    }
}

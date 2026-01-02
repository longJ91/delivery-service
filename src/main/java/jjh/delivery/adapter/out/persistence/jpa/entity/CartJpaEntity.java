package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart JPA Entity
 */
@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_carts_customer_id", columnList = "customer_id", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "customer_id", nullable = false, unique = true, length = 36)
    private String customerId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("addedAt DESC")
    private List<CartItemJpaEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public CartJpaEntity(String id, String customerId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Setters
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Item management
    public void addItem(CartItemJpaEntity item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItemJpaEntity item) {
        items.remove(item);
        item.setCart(null);
    }

    public void clearItems() {
        items.forEach(item -> item.setCart(null));
        items.clear();
    }
}

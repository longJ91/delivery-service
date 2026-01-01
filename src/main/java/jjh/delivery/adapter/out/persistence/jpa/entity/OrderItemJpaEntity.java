package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Order Item JPA Entity
 */
@Entity
@Table(name = "order_items")
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(name = "menu_id", nullable = false, length = 36)
    private String menuId;

    @Column(name = "menu_name", nullable = false, length = 200)
    private String menuName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    protected OrderItemJpaEntity() {
    }

    public OrderItemJpaEntity(
            String menuId,
            String menuName,
            int quantity,
            BigDecimal unitPrice
    ) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void setOrder(OrderJpaEntity order) {
        this.order = order;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public OrderJpaEntity getOrder() {
        return order;
    }

    public String getMenuId() {
        return menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}

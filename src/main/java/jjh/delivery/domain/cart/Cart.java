package jjh.delivery.domain.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Cart Aggregate Root
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class Cart {

    private final String id;
    private final String customerId;
    private final List<CartItem> items;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Cart(String id, String customerId, List<CartItem> items, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * 새 장바구니 생성
     */
    public static Cart createEmpty(String customerId) {
        return new Cart(
                java.util.UUID.randomUUID().toString(),
                customerId,
                new ArrayList<>(),
                LocalDateTime.now()
        );
    }

    /**
     * 기존 장바구니 복원
     */
    public static Cart restore(String id, String customerId, List<CartItem> items, LocalDateTime createdAt) {
        return new Cart(id, customerId, items, createdAt);
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 상품 추가
     */
    public CartItem addItem(
            String productId,
            String productName,
            String variantId,
            String variantName,
            String sellerId,
            int quantity,
            BigDecimal unitPrice,
            String thumbnailUrl
    ) {
        // 동일 상품+옵션이 이미 있으면 수량 증가
        Optional<CartItem> existing = findItemByProductAndVariant(productId, variantId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            CartItem updated = item.updateQuantity(item.quantity() + quantity);
            items.replaceAll(i -> i.id().equals(item.id()) ? updated : i);
            this.updatedAt = LocalDateTime.now();
            return updated;
        }

        // 새 항목 추가
        CartItem newItem = CartItem.create(
                productId,
                productName,
                variantId,
                variantName,
                sellerId,
                quantity,
                unitPrice,
                thumbnailUrl
        );
        items.add(newItem);
        this.updatedAt = LocalDateTime.now();
        return newItem;
    }

    /**
     * 수량 변경
     */
    public CartItem updateItemQuantity(String itemId, int quantity) {
        CartItem item = findItemById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + itemId));

        CartItem updated = item.updateQuantity(quantity);
        items.replaceAll(i -> i.id().equals(itemId) ? updated : i);
        this.updatedAt = LocalDateTime.now();
        return updated;
    }

    /**
     * 상품 제거
     */
    public void removeItem(String itemId) {
        boolean removed = items.removeIf(item -> item.id().equals(itemId));
        if (!removed) {
            throw new IllegalArgumentException("Cart item not found: " + itemId);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 장바구니 비우기
     */
    public void clear() {
        items.clear();
        this.updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 총 금액 계산
     */
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 총 상품 수
     */
    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::quantity)
                .sum();
    }

    /**
     * 장바구니가 비어있는지 확인
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * 항목 조회
     */
    public Optional<CartItem> findItemById(String itemId) {
        return items.stream()
                .filter(item -> item.id().equals(itemId))
                .findFirst();
    }

    /**
     * 상품+옵션으로 항목 조회
     */
    public Optional<CartItem> findItemByProductAndVariant(String productId, String variantId) {
        return items.stream()
                .filter(item -> item.productId().equals(productId) &&
                        (variantId == null ? item.variantId() == null : variantId.equals(item.variantId())))
                .findFirst();
    }

    // =====================================================
    // Getters
    // =====================================================

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

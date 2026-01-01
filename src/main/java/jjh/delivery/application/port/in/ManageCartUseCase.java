package jjh.delivery.application.port.in;

import jjh.delivery.domain.cart.Cart;
import jjh.delivery.domain.cart.CartItem;

import java.math.BigDecimal;

/**
 * 장바구니 관리 Use Case - Driving Port (Inbound)
 */
public interface ManageCartUseCase {

    /**
     * 장바구니 조회
     */
    Cart getCart(String customerId);

    /**
     * 상품 추가
     */
    CartItem addItem(String customerId, AddCartItemCommand command);

    /**
     * 수량 변경
     */
    CartItem updateItemQuantity(String customerId, String itemId, int quantity);

    /**
     * 상품 제거
     */
    void removeItem(String customerId, String itemId);

    /**
     * 장바구니 비우기
     */
    void clearCart(String customerId);

    /**
     * 상품 추가 커맨드
     */
    record AddCartItemCommand(
            String productId,
            String variantId,
            int quantity
    ) {}
}

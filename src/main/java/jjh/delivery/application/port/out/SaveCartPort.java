package jjh.delivery.application.port.out;

import jjh.delivery.domain.cart.Cart;

import java.util.UUID;

/**
 * Cart 저장 Port - Driven Port (Outbound)
 */
public interface SaveCartPort {

    /**
     * 장바구니 저장
     */
    Cart save(Cart cart);

    /**
     * 장바구니 삭제
     */
    void deleteByCustomerId(UUID customerId);
}

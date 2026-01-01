package jjh.delivery.application.port.out;

import jjh.delivery.domain.cart.Cart;

import java.util.Optional;

/**
 * Cart 조회 Port - Driven Port (Outbound)
 */
public interface LoadCartPort {

    /**
     * 고객 ID로 장바구니 조회
     */
    Optional<Cart> findByCustomerId(String customerId);

    /**
     * 장바구니 존재 여부 확인
     */
    boolean existsByCustomerId(String customerId);
}

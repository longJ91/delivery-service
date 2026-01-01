package jjh.delivery.domain.cart.exception;

/**
 * 장바구니 항목을 찾을 수 없을 때 발생하는 예외
 */
public class CartItemNotFoundException extends RuntimeException {

    private final String itemId;

    public CartItemNotFoundException(String itemId) {
        super("Cart item not found: " + itemId);
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }
}

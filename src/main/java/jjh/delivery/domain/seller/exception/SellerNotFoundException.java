package jjh.delivery.domain.seller.exception;

/**
 * Exception thrown when seller is not found
 */
public class SellerNotFoundException extends RuntimeException {

    private final String sellerId;

    public SellerNotFoundException(String sellerId) {
        super("Seller not found: " + sellerId);
        this.sellerId = sellerId;
    }

    public String getSellerId() {
        return sellerId;
    }
}

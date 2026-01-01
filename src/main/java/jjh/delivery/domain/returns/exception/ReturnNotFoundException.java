package jjh.delivery.domain.returns.exception;

/**
 * Exception thrown when return is not found
 */
public class ReturnNotFoundException extends RuntimeException {

    private final String returnId;

    public ReturnNotFoundException(String returnId) {
        super("Return not found: " + returnId);
        this.returnId = returnId;
    }

    public String getReturnId() {
        return returnId;
    }
}

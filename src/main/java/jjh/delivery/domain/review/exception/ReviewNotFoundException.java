package jjh.delivery.domain.review.exception;

/**
 * Exception thrown when review is not found
 */
public class ReviewNotFoundException extends RuntimeException {

    private final String reviewId;

    public ReviewNotFoundException(String reviewId) {
        super("Review not found: " + reviewId);
        this.reviewId = reviewId;
    }

    public String getReviewId() {
        return reviewId;
    }
}

package jjh.delivery.application.port.out;

import jjh.delivery.domain.review.Review;

/**
 * Review Save Port - Driven Port (Outbound)
 */
public interface SaveReviewPort {

    Review save(Review review);

    void delete(String reviewId);
}

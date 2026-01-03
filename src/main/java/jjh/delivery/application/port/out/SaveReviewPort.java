package jjh.delivery.application.port.out;

import jjh.delivery.domain.review.Review;

import java.util.UUID;

/**
 * Review Save Port - Driven Port (Outbound)
 */
public interface SaveReviewPort {

    Review save(Review review);

    void delete(UUID reviewId);
}

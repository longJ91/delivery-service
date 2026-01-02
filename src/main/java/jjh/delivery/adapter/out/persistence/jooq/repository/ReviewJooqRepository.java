package jjh.delivery.adapter.out.persistence.jooq.repository;

import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.ReviewImagesRecord;
import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.ReviewRepliesRecord;
import jjh.delivery.adapter.out.persistence.jooq.generated.tables.records.ReviewsRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.ReviewImages.REVIEW_IMAGES;
import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.ReviewReplies.REVIEW_REPLIES;
import static jjh.delivery.adapter.out.persistence.jooq.generated.tables.Reviews.REVIEWS;
import static org.jooq.impl.DSL.*;

/**
 * Review jOOQ Repository - Type-safe queries
 * Replaces @Query methods in ReviewJpaRepository
 */
@Repository
@RequiredArgsConstructor
public class ReviewJooqRepository {

    private final DSLContext dsl;

    /**
     * Find review by ID with details (replaces findByIdWithDetails)
     * Compile-time type-safe version of:
     * SELECT DISTINCT r FROM ReviewJpaEntity r
     * LEFT JOIN FETCH r.images LEFT JOIN FETCH r.reply WHERE r.id = :id
     */
    public Optional<ReviewWithDetails> findByIdWithDetails(String id) {
        Result<Record> result = dsl
                .select()
                .from(REVIEWS)
                .leftJoin(REVIEW_IMAGES)
                    .on(REVIEW_IMAGES.REVIEW_ID.eq(REVIEWS.ID))
                .leftJoin(REVIEW_REPLIES)
                    .on(REVIEW_REPLIES.REVIEW_ID.eq(REVIEWS.ID))
                .where(REVIEWS.ID.eq(id))
                .fetch();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToReviewWithDetails(result));
    }

    /**
     * Get average rating by product ID (replaces getAverageRatingByProductId)
     * Compile-time type-safe version of:
     * SELECT COALESCE(AVG(r.rating), 0.0) FROM ReviewJpaEntity r
     * WHERE r.productId = :productId AND r.isVisible = true
     */
    public double getAverageRatingByProductId(String productId) {
        BigDecimal avg = dsl
                .select(coalesce(avg(REVIEWS.RATING), BigDecimal.ZERO))
                .from(REVIEWS)
                .where(REVIEWS.PRODUCT_ID.eq(productId))
                .and(REVIEWS.IS_VISIBLE.eq(true))
                .fetchOneInto(BigDecimal.class);

        return avg != null ? avg.doubleValue() : 0.0;
    }

    /**
     * Get rating distribution by product ID (replaces getRatingDistributionByProductId)
     * Compile-time type-safe version of:
     * SELECT r.rating, COUNT(r) FROM ReviewJpaEntity r
     * WHERE r.productId = :productId AND r.isVisible = true GROUP BY r.rating
     */
    public Map<Integer, Long> getRatingDistributionByProductId(String productId) {
        Result<Record2<Integer, Integer>> result = dsl
                .select(REVIEWS.RATING, count())
                .from(REVIEWS)
                .where(REVIEWS.PRODUCT_ID.eq(productId))
                .and(REVIEWS.IS_VISIBLE.eq(true))
                .groupBy(REVIEWS.RATING)
                .fetch();

        Map<Integer, Long> distribution = new HashMap<>();
        for (Record2<Integer, Integer> record : result) {
            distribution.put(record.value1(), record.value2().longValue());
        }

        return distribution;
    }

    /**
     * Helper method to map result to ReviewWithDetails
     */
    private ReviewWithDetails mapToReviewWithDetails(Result<Record> result) {
        ReviewsRecord review = result.get(0).into(REVIEWS);

        List<ReviewImagesRecord> images = result.stream()
                .filter(r -> r.get(REVIEW_IMAGES.ID) != null)
                .map(r -> r.into(REVIEW_IMAGES))
                .distinct()
                .toList();

        ReviewRepliesRecord reply = result.stream()
                .filter(r -> r.get(REVIEW_REPLIES.ID) != null)
                .map(r -> r.into(REVIEW_REPLIES))
                .findFirst()
                .orElse(null);

        return new ReviewWithDetails(review, images, reply);
    }

    /**
     * Result DTO for review with details
     */
    public record ReviewWithDetails(
            ReviewsRecord review,
            List<ReviewImagesRecord> images,
            ReviewRepliesRecord reply
    ) {}
}

package jjh.delivery.domain.review;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Review Reply Value Object
 * Seller's response to a customer review
 */
public record ReviewReply(
        UUID id,
        UUID sellerId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewReply of(UUID id, UUID sellerId, String content,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new ReviewReply(id, sellerId, content, createdAt, updatedAt);
    }

    public static ReviewReply create(UUID sellerId, String content) {
        LocalDateTime now = LocalDateTime.now();
        return new ReviewReply(UUID.randomUUID(), sellerId, content, now, now);
    }

    public ReviewReply update(String newContent) {
        return new ReviewReply(this.id, this.sellerId, newContent, this.createdAt, LocalDateTime.now());
    }
}

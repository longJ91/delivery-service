package jjh.delivery.domain.review;

import java.util.UUID;

/**
 * Review Image Value Object
 */
public record ReviewImage(
        UUID id,
        String imageUrl,
        int displayOrder
) {
    public static ReviewImage of(UUID id, String imageUrl, int displayOrder) {
        return new ReviewImage(id, imageUrl, displayOrder);
    }

    public static ReviewImage ofNew(UUID id, String imageUrl, int displayOrder) {
        return new ReviewImage(id, imageUrl, displayOrder);
    }
}

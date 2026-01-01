package jjh.delivery.domain.review;

/**
 * Review Image Value Object
 */
public record ReviewImage(
        String id,
        String imageUrl,
        int displayOrder
) {
    public static ReviewImage of(String id, String imageUrl, int displayOrder) {
        return new ReviewImage(id, imageUrl, displayOrder);
    }

    public static ReviewImage ofNew(String imageUrl, int displayOrder) {
        return new ReviewImage(null, imageUrl, displayOrder);
    }
}

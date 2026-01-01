package jjh.delivery.adapter.in.web.review.dto;

import jjh.delivery.domain.review.Review;
import jjh.delivery.domain.review.ReviewImage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 상세 응답
 */
public record ReviewDetailResponse(
        String id,
        String orderId,
        String customerId,
        String sellerId,
        String productId,
        int rating,
        String content,
        List<String> imageUrls,
        ReviewReplyResponse reply,
        boolean hasReply,
        boolean hasImages,
        int imageCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewDetailResponse from(Review review) {
        List<String> imageUrls = review.getImages().stream()
                .map(ReviewImage::imageUrl)
                .toList();

        return new ReviewDetailResponse(
                review.getId(),
                review.getOrderId(),
                review.getCustomerId(),
                review.getSellerId(),
                review.getProductId(),
                review.getRating(),
                review.getContent(),
                imageUrls,
                ReviewReplyResponse.from(review.getReply()),
                review.hasReply(),
                review.hasImages(),
                review.getImageCount(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}

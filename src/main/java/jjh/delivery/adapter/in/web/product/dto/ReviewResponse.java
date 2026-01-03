package jjh.delivery.adapter.in.web.product.dto;

import jjh.delivery.domain.review.Review;
import jjh.delivery.domain.review.ReviewImage;
import jjh.delivery.domain.review.ReviewReply;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 리뷰 응답
 */
public record ReviewResponse(
        String id,
        String customerId,
        String customerName,
        int rating,
        String content,
        List<ReviewImageResponse> images,
        ReviewReplyResponse reply,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review, String customerName) {
        return new ReviewResponse(
                review.getId().toString(),
                review.getCustomerId().toString(),
                maskCustomerName(customerName),
                review.getRating(),
                review.getContent(),
                review.getImages().stream()
                        .map(ReviewImageResponse::from)
                        .toList(),
                review.getReply() != null ? ReviewReplyResponse.from(review.getReply()) : null,
                review.getCreatedAt()
        );
    }

    private static String maskCustomerName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        // 이름 마스킹: 홍길동 -> 홍*동
        char first = name.charAt(0);
        char last = name.charAt(name.length() - 1);
        StringBuilder masked = new StringBuilder();
        masked.append(first);
        for (int i = 1; i < name.length() - 1; i++) {
            masked.append("*");
        }
        masked.append(last);
        return masked.toString();
    }

    public record ReviewImageResponse(
            String id,
            String imageUrl
    ) {
        public static ReviewImageResponse from(ReviewImage image) {
            return new ReviewImageResponse(image.id().toString(), image.imageUrl());
        }
    }

    public record ReviewReplyResponse(
            String content,
            LocalDateTime createdAt
    ) {
        public static ReviewReplyResponse from(ReviewReply reply) {
            return new ReviewReplyResponse(reply.content(), reply.createdAt());
        }
    }
}

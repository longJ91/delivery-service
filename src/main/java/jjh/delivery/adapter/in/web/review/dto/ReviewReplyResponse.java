package jjh.delivery.adapter.in.web.review.dto;

import jjh.delivery.domain.review.ReviewReply;

import java.time.LocalDateTime;

/**
 * 리뷰 답글 응답
 */
public record ReviewReplyResponse(
        String id,
        String sellerId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewReplyResponse from(ReviewReply reply) {
        if (reply == null) {
            return null;
        }
        return new ReviewReplyResponse(
                reply.id(),
                reply.sellerId(),
                reply.content(),
                reply.createdAt(),
                reply.updatedAt()
        );
    }
}

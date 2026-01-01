package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.ReviewImageJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.ReviewJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.ReviewReplyJpaEntity;
import jjh.delivery.domain.review.Review;
import jjh.delivery.domain.review.ReviewImage;
import jjh.delivery.domain.review.ReviewReply;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Review 영속성 매퍼
 */
@Component
public class ReviewPersistenceMapper {

    public Review toDomain(ReviewJpaEntity entity) {
        return Review.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .customerId(entity.getCustomerId())
                .sellerId(entity.getSellerId())
                .productId(entity.getProductId())
                .rating(entity.getRating())
                .content(entity.getContent())
                .images(entity.getImages().stream()
                        .map(this::toDomainImage)
                        .collect(Collectors.toList()))
                .reply(entity.getReply() != null ? toDomainReply(entity.getReply()) : null)
                .isVisible(entity.isVisible())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private ReviewImage toDomainImage(ReviewImageJpaEntity entity) {
        return ReviewImage.of(entity.getId(), entity.getImageUrl(), entity.getDisplayOrder());
    }

    private ReviewReply toDomainReply(ReviewReplyJpaEntity entity) {
        return ReviewReply.of(
                entity.getId(),
                entity.getSellerId(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public ReviewJpaEntity toEntity(Review domain) {
        ReviewJpaEntity entity = new ReviewJpaEntity(
                domain.getId(),
                domain.getOrderId(),
                domain.getCustomerId(),
                domain.getSellerId(),
                domain.getProductId(),
                domain.getRating(),
                domain.getContent(),
                domain.isVisible(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );

        for (ReviewImage image : domain.getImages()) {
            entity.addImage(toEntityImage(image));
        }

        if (domain.getReply() != null) {
            entity.setReply(toEntityReply(domain.getReply()));
        }

        return entity;
    }

    private ReviewImageJpaEntity toEntityImage(ReviewImage domain) {
        return new ReviewImageJpaEntity(
                domain.id(),
                domain.imageUrl(),
                domain.displayOrder(),
                java.time.LocalDateTime.now()
        );
    }

    private ReviewReplyJpaEntity toEntityReply(ReviewReply domain) {
        return new ReviewReplyJpaEntity(
                domain.id(),
                domain.sellerId(),
                domain.content(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}

package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Review Image JPA Entity
 */
@Entity
@Table(name = "review_images", indexes = {
        @Index(name = "idx_review_images_review", columnList = "review_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImageJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ReviewJpaEntity review;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ReviewImageJpaEntity(
            String id,
            String imageUrl,
            int displayOrder,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
    }

    // Setters
    public void setReview(ReviewJpaEntity review) {
        this.review = review;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
